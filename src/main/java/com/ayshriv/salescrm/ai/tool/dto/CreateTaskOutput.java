package com.ayshriv.salescrm.ai.tool.dto;

import java.io.Serializable;

public class CreateTaskOutput implements Serializable {

    private boolean success;
    private Long taskId;
    private String title;
    private String status;
    private String priority;
    private String message;

    public CreateTaskOutput() {
    }

    public CreateTaskOutput(boolean success, Long taskId, String title, String status, String priority, String message) {
        this.success = success;
        this.taskId = taskId;
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}