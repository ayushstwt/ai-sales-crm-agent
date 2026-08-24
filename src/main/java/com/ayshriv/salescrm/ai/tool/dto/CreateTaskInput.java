package com.ayshriv.salescrm.ai.tool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class CreateTaskInput {

    @JsonProperty("title")
    @JsonPropertyDescription("The task title or description of action required")
    private String title;

    @JsonProperty("description")
    @JsonPropertyDescription("Detailed instructions or notes for the task")
    private String description;

    @JsonProperty("dueDate")
    @JsonPropertyDescription("Due date in ISO format (e.g. 2026-08-30T10:00:00)")
    private String dueDate;

    @JsonProperty("priority")
    @JsonPropertyDescription("Priority level: LOW, MEDIUM, HIGH, URGENT (default: MEDIUM)")
    private String priority;

    @JsonProperty("status")
    @JsonPropertyDescription("Status: PENDING, IN_PROGRESS, COMPLETED, CANCELLED (default: PENDING)")
    private String status;

    @JsonProperty("assignedToId")
    @JsonPropertyDescription("User ID to assign this task to (optional)")
    private Long assignedToId;

    @JsonProperty("relatedType")
    @JsonPropertyDescription("Type of related entity: LEAD, CONTACT, COMPANY, DEAL (optional)")
    private String relatedType;

    @JsonProperty("relatedId")
    @JsonPropertyDescription("ID of related entity (optional)")
    private Long relatedId;

    public CreateTaskInput() {
    }

    public CreateTaskInput(String title, String description, String priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
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

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public String getRelatedType() {
        return relatedType;
    }

    public void setRelatedType(String relatedType) {
        this.relatedType = relatedType;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }
}