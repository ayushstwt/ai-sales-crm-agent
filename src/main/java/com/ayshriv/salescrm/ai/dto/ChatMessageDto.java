package com.ayshriv.salescrm.ai.dto;

import com.ayshriv.salescrm.ai.entity.MessageRole;
import java.io.Serializable;
import java.time.LocalDateTime;

public class ChatMessageDto implements Serializable {

    private Long id;
    private Long conversationId;
    private MessageRole role;
    private String content;
    private LocalDateTime createdOn;

    public ChatMessageDto() {
    }

    public ChatMessageDto(Long id, Long conversationId, MessageRole role, String content, LocalDateTime createdOn) {
        this.id = id;
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.createdOn = createdOn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public MessageRole getRole() {
        return role;
    }

    public void setRole(MessageRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }
}
