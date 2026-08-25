package com.ayshriv.salescrm.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class PendingActionPayload {

    private String actionType;
    private int count;
    private List<Long> targetIds = new ArrayList<>();
    private String description;

    public PendingActionPayload() {
    }

    public PendingActionPayload(String actionType, int count, List<Long> targetIds, String description) {
        this.actionType = actionType;
        this.count = count;
        this.targetIds = targetIds != null ? targetIds : new ArrayList<>();
        this.description = description;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Long> getTargetIds() {
        return targetIds;
    }

    public void setTargetIds(List<Long> targetIds) {
        this.targetIds = targetIds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
