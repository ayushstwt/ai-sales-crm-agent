package com.ayshriv.salescrm.ai.service.impl;

import com.ayshriv.salescrm.ai.service.LLMProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Primary
public class OpenAIProvider implements LLMProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAIProvider.class);

    private final ChatModel chatModel;

    public OpenAIProvider(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String generateText(String prompt) {
        LOGGER.info("OpenAIProvider >> generateText called with prompt: {}", prompt);
        if (prompt == null || prompt.isBlank()) {
            return "";
        }
        return chatModel.call(prompt);
    }

    @Override
    public String generateText(List<Message> messages) {
        LOGGER.info("OpenAIProvider >> generateText called with {} messages", messages != null ? messages.size() : 0);
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        Prompt prompt = new Prompt(messages);
        org.springframework.ai.chat.model.ChatResponse response = chatModel.call(prompt);
        if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
            return response.getResult().getOutput().getContent();
        }
        return "";
    }

    @Override
    public String generateText(String systemPrompt, String userPrompt) {
        LOGGER.info("OpenAIProvider >> generateText called with system and user prompts");
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
        LOGGER.info("OpenAIProvider >> generateTextWithTools called with {} messages and {} tools",
                messages != null ? messages.size() : 0, toolCallbacks != null ? toolCallbacks.size() : 0);
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        if (toolCallbacks == null || toolCallbacks.isEmpty()) {
            return generateText(messages);
        }

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withFunctionCallbacks(toolCallbacks)
                .build();

        Prompt prompt = new Prompt(messages, options);
        org.springframework.ai.chat.model.ChatResponse response = chatModel.call(prompt);
        if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
            return response.getResult().getOutput().getContent();
        }
        return "";
    }
}