package com.ayshriv.salescrm.ai.tool.dto;

import java.util.List;

public class BulkDeleteLeadsOutput {

    private boolean requiresConfirmation;
    private int count;
    private List<Long> leadIds;
    private List<LeadSummary> preview;
    private String message;

    public BulkDeleteLeadsOutput() {
    }

    public BulkDeleteLeadsOutput(boolean requiresConfirmation, int count, List<Long> leadIds, List<LeadSummary> preview, String message) {
        this.requiresConfirmation = requiresConfirmation;
        this.count = count;
        this.leadIds = leadIds;
        this.preview = preview;
        this.message = message;
    }

    public boolean isRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Long> getLeadIds() {
        return leadIds;
    }

    public void setLeadIds(List<Long> leadIds) {
        this.leadIds = leadIds;
    }

    public List<LeadSummary> getPreview() {
        return preview;
    }

    public void setPreview(List<LeadSummary> preview) {
        this.preview = preview;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
