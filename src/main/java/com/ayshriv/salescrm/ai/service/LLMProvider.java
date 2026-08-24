package com.ayshriv.salescrm.ai.service;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.model.function.FunctionCallback;

import java.util.List;

public interface LLMProvider {

    String generateText(String prompt);

    String generateText(List<Message> messages);

    String generateText(String systemPrompt, String userPrompt);

    String generateTextWithTools(List<Message> messages, List<FunctionCallback> toolCallbacks);
}