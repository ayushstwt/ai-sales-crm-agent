package com.ayshriv.salescrm.ai.service.impl;

import com.ayshriv.salescrm.ai.service.LLMProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Production Azure OpenAI provider with native function/tool calling support.
 * Configured via AZURE_OPENAI_API_KEY, AZURE_OPENAI_ENDPOINT, and AZURE_OPENAI_DEPLOYMENT.
 */
@Service("azureOpenAIProvider")
@Primary
public class AzureOpenAIProvider implements LLMProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureOpenAIProvider.class);
    private static final String API_VERSION = "2024-06-01";
    private static final int MAX_TOOL_ITERATIONS = 10;

    @Value("${azure.openai.api-key:}")
    private String apiKey;

    @Value("${azure.openai.endpoint:https://sharkdom-aditya-openai.openai.azure.com/}")
    private String endpoint;

    @Value("${azure.openai.deployment:gpt-5-mini}")
    private String deployment;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public AzureOpenAIProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public String generateText(String prompt) {
        LOGGER.info("AzureOpenAIProvider >> generateText called with single prompt");
        if (prompt == null || prompt.isBlank()) {
            return "";
        }
        return generateText(List.of(new UserMessage(prompt)));
    }

    @Override
    public String generateText(List<Message> messages) {
        LOGGER.info("AzureOpenAIProvider >> generateText called with {} messages", messages != null ? messages.size() : 0);
        return generateTextWithTools(messages, List.of());
    }

    @Override
    public String generateText(String systemPrompt, String userPrompt) {
        LOGGER.info("AzureOpenAIProvider >> generateText called with system and user prompts");
        List<Message> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new SystemMessage(systemPrompt));
        }
        if (userPrompt != null && !userPrompt.isBlank()) {
            messages.add(new UserMessage(userPrompt));
        }
        return generateText(messages);
    }

    @Override
    public String generateTextWithTools(List<Message> messages, List<FunctionCallback> toolCallbacks) {
        LOGGER.info("AzureOpenAIProvider >> generateTextWithTools called on deployment '{}' with {} messages and {} tools",
                deployment, messages != null ? messages.size() : 0, toolCallbacks != null ? toolCallbacks.size() : 0);

        if (messages == null || messages.isEmpty()) {
            return "";
        }

        try {
            String baseUrl = sanitizeEndpoint(endpoint);
            String url = String.format("%s/openai/deployments/%s/chat/completions?api-version=%s", baseUrl, deployment, API_VERSION);

            ArrayNode messagesArray = objectMapper.createArrayNode();
            for (Message msg : messages) {
                ObjectNode node = messagesArray.addObject();
                if (msg instanceof SystemMessage) {
                    node.put("role", "system");
                } else if (msg instanceof AssistantMessage) {
                    node.put("role", "assistant");
                } else {
                    node.put("role", "user");
                }
                node.put("content", msg.getContent());
            }

            ArrayNode toolsArray = null;
            if (toolCallbacks != null && !toolCallbacks.isEmpty()) {
                toolsArray = objectMapper.createArrayNode();
                for (FunctionCallback callback : toolCallbacks) {
                    ObjectNode toolNode = toolsArray.addObject();
                    toolNode.put("type", "function");
                    ObjectNode fnNode = toolNode.putObject("function");
                    fnNode.put("name", callback.getName());
                    fnNode.put("description", callback.getDescription());
                    try {
                        JsonNode schemaNode = objectMapper.readTree(callback.getInputTypeSchema());
                        fnNode.set("parameters", schemaNode);
                    } catch (Exception e) {
                        fnNode.putObject("parameters").put("type", "object");
                    }
                }
            }

            int iteration = 0;
            while (iteration < MAX_TOOL_ITERATIONS) {
                iteration++;

                ObjectNode requestBody = objectMapper.createObjectNode();
                requestBody.set("messages", messagesArray);
                if (toolsArray != null && !toolsArray.isEmpty()) {
                    requestBody.set("tools", toolsArray);
                }

                LOGGER.info("AzureOpenAIProvider >> Sending chat completion request to Azure OpenAI (iteration {})", iteration);

                String responseBody = restClient.post()
                        .uri(url)
                        .header("api-key", apiKey)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(objectMapper.writeValueAsString(requestBody))
                        .retrieve()
                        .body(String.class);

                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode choices = root.path("choices");
                if (choices.isEmpty()) {
                    LOGGER.warn("AzureOpenAIProvider >> No choices returned by Azure OpenAI");
                    return "";
                }

                JsonNode firstChoice = choices.get(0);
                JsonNode choiceMessage = firstChoice.path("message");
                String finishReason = firstChoice.path("finish_reason").asText();
                String content = choiceMessage.path("content").asText("");

                JsonNode toolCalls = choiceMessage.path("tool_calls");
                if ("tool_calls".equals(finishReason) || (toolCalls.isArray() && !toolCalls.isEmpty())) {
                    // Append assistant message with tool calls
                    ObjectNode assistantWithToolCalls = messagesArray.addObject();
                    assistantWithToolCalls.put("role", "assistant");
                    if (content != null && !content.isBlank()) {
                        assistantWithToolCalls.put("content", content);
                    } else {
                        assistantWithToolCalls.putNull("content");
                    }
                    assistantWithToolCalls.set("tool_calls", toolCalls);

                    // Execute each tool call and append tool response
                    for (JsonNode tc : toolCalls) {
                        String toolCallId = tc.path("id").asText();
                        String fnName = tc.path("function").path("name").asText();
                        String fnArgs = tc.path("function").path("arguments").asText("{}");

                        LOGGER.info("AzureOpenAIProvider >> Executing tool callback: {} with args: {}", fnName, fnArgs);

                        String toolResult = "";
                        FunctionCallback callback = findCallback(toolCallbacks, fnName);
                        if (callback != null) {
                            try {
                                toolResult = callback.call(fnArgs);
                            } catch (Exception e) {
                                LOGGER.error("AzureOpenAIProvider >> Tool execution error for {}: {}", fnName, e.getMessage());
                                toolResult = "{\"error\":\"" + e.getMessage() + "\"}";
                            }
                        } else {
                            LOGGER.warn("AzureOpenAIProvider >> No matching callback found for tool: {}", fnName);
                            toolResult = "{\"error\":\"Tool not found\"}";
                        }

                        ObjectNode toolResponseNode = messagesArray.addObject();
                        toolResponseNode.put("role", "tool");
                        toolResponseNode.put("tool_call_id", toolCallId);
                        toolResponseNode.put("name", fnName);
                        toolResponseNode.put("content", toolResult != null ? toolResult : "");
                    }

                    // Continue loop to get next completion from Azure with tool outputs
                    continue;
                }

                // Final text response reached
                return content;
            }

            LOGGER.warn("AzureOpenAIProvider >> Exceeded maximum tool iterations ({})", MAX_TOOL_ITERATIONS);
            return "";

        } catch (Exception e) {
            LOGGER.error("AzureOpenAIProvider >> Failed to generate text via Azure OpenAI: {}", e.getMessage(), e);
            throw new RuntimeException("Azure OpenAI completion failed: " + e.getMessage(), e);
        }
    }

    private FunctionCallback findCallback(List<FunctionCallback> callbacks, String name) {
        if (callbacks == null || name == null) return null;
        return callbacks.stream()
                .filter(c -> name.equalsIgnoreCase(c.getName()))
                .findFirst()
                .orElse(null);
    }

    private String sanitizeEndpoint(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}