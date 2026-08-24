package com.ayshriv.salescrm.ai.tool.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SearchDealsOutput implements Serializable {

    private int count;
    private long total;
    private List<DealSummary> deals = new ArrayList<>();
    private String message;

    public SearchDealsOutput() {
    }

    public SearchDealsOutput(int count, long total, List<DealSummary> deals, String message) {
        this.count = count;
        this.total = total;
        this.deals = deals != null ? deals : new ArrayList<>();
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

    public List<DealSummary> getDeals() {
        return deals;
    }

    public void setDeals(List<DealSummary> deals) {
        this.deals = deals != null ? deals : new ArrayList<>();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}