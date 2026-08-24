package com.ayshriv.salescrm.ai.service;

import com.ayshriv.salescrm.ai.dto.ChatMessageDto;
import com.ayshriv.salescrm.ai.dto.ChatRequest;
import com.ayshriv.salescrm.ai.dto.ChatResponse;

import java.util.List;

public interface AiChatService {

    ChatResponse chat(ChatRequest request);

    String getRawCompletion(String prompt);

    List<ChatMessageDto> getConversationMessages(Long conversationId);
}
