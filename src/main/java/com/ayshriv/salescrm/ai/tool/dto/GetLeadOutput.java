package com.ayshriv.salescrm.ai.tool.dto;

import java.io.Serializable;

public class GetLeadOutput implements Serializable {

    private boolean found;
    private LeadSummary lead;
    private String message;

    public GetLeadOutput() {
    }

    public GetLeadOutput(boolean found, LeadSummary lead, String message) {
        this.found = found;
        this.lead = lead;
        this.message = message;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public LeadSummary getLead() {
        return lead;
    }

    public void setLead(LeadSummary lead) {
        this.lead = lead;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}