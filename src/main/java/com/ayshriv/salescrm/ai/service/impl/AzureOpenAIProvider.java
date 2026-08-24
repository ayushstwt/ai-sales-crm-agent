package com.ayshriv.salescrm.ai.service.impl;

import com.ayshriv.salescrm.ai.service.LLMProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Stub implementation for Azure OpenAI provider per master.md decision #1.
 */
@Service("azureOpenAIProvider")
public class AzureOpenAIProvider implements LLMProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureOpenAIProvider.class);

    @Override
    public String generateText(String prompt) {
        LOGGER.warn("AzureOpenAIProvider >> generateText called, but Azure OpenAI is currently a stub.");
        throw new UnsupportedOperationException("Azure OpenAI provider is not yet implemented (stub per master.md decision #1).");
    }

    @Override
    public String generateText(List<Message> messages) {
        LOGGER.warn("AzureOpenAIProvider >> generateText called, but Azure OpenAI is currently a stub.");
        throw new UnsupportedOperationException("Azure OpenAI provider is not yet implemented (stub per master.md decision #1).");
    }

    @Override
    public String generateText(String systemPrompt, String userPrompt) {
        LOGGER.warn("AzureOpenAIProvider >> generateText called, but Azure OpenAI is currently a stub.");
        throw new UnsupportedOperationException("Azure OpenAI provider is not yet implemented (stub per master.md decision #1).");
    }

    @Override
    public String generateTextWithTools(List<Message> messages, List<FunctionCallback> toolCallbacks) {
        LOGGER.warn("AzureOpenAIProvider >> generateTextWithTools called, but Azure OpenAI is currently a stub.");
        throw new UnsupportedOperationException("Azure OpenAI provider is not yet implemented (stub per master.md decision #1).");
    }
}