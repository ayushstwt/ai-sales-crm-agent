package com.ayshriv.salescrm.task.dto;

import com.ayshriv.salescrm.common.dto.BaseSearchRequest;
import com.ayshriv.salescrm.task.entity.TaskPriority;
import com.ayshriv.salescrm.task.entity.TaskStatus;

public class TaskSearchRequest extends BaseSearchRequest {

    private Long organizationId;
    private Long assignedToId;
    private TaskStatus status;
    private TaskPriority priority;
    private String relatedType;
    private Long relatedId;

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
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