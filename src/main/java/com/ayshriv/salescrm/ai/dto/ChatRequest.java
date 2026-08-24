package com.ayshriv.salescrm.ai.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public class ChatRequest implements Serializable {

    private Long conversationId;

    @NotBlank(message = "Message cannot be blank")
    private String message;

    public ChatRequest() {
    }

    public ChatRequest(String message) {
        this.message = message;
    }

    public ChatRequest(Long conversationId, String message) {
        this.conversationId = conversationId;
        this.message = message;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
