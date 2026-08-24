package com.ayshriv.salescrm.ai.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatResponse implements Serializable {

    private Long conversationId;
    private String message;
    private String role;
    private List<ToolCallDto> toolCalls = new ArrayList<>();
    private LocalDateTime createdOn;

    public ChatResponse() {
    }

    public ChatResponse(Long conversationId, String message, String role, LocalDateTime createdOn) {
        this.conversationId = conversationId;
        this.message = message;
        this.role = role;
        this.createdOn = createdOn;
        this.toolCalls = new ArrayList<>();
    }

    public ChatResponse(Long conversationId, String message, String role, List<ToolCallDto> toolCalls, LocalDateTime createdOn) {
        this.conversationId = conversationId;
        this.message = message;
        this.role = role;
        this.toolCalls = toolCalls != null ? toolCalls : new ArrayList<>();
        this.createdOn = createdOn;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<ToolCallDto> getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(List<ToolCallDto> toolCalls) {
        this.toolCalls = toolCalls != null ? toolCalls : new ArrayList<>();
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }
}
