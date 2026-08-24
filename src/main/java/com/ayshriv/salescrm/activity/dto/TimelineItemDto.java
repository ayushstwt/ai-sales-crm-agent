package com.ayshriv.salescrm.activity.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TimelineItemDto implements Serializable {

    private Long id;
    private String eventType;
    private String title;
    private String description;
    private String source;
    private String userName;
    private LocalDateTime timestamp;

    public TimelineItemDto() {
    }

    public TimelineItemDto(Long id, String eventType, String title, String description, String source, String userName, LocalDateTime timestamp) {
        this.id = id;
        this.eventType = eventType;
        this.title = title;
        this.description = description;
        this.source = source;
        this.userName = userName;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}