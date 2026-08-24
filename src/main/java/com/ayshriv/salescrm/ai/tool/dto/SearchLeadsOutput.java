package com.ayshriv.salescrm.ai.tool.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SearchLeadsOutput implements Serializable {

    private int count;
    private long total;
    private List<LeadSummary> leads = new ArrayList<>();
    private String message;

    public SearchLeadsOutput() {
    }

    public SearchLeadsOutput(int count, long total, List<LeadSummary> leads, String message) {
        this.count = count;
        this.total = total;
        this.leads = leads != null ? leads : new ArrayList<>();
        this.message = message;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<LeadSummary> getLeads() {
        return leads;
    }

    public void setLeads(List<LeadSummary> leads) {
        this.leads = leads != null ? leads : new ArrayList<>();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}