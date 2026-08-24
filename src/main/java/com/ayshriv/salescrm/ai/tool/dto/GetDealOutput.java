package com.ayshriv.salescrm.ai.tool.dto;

import java.io.Serializable;

public class GetDealOutput implements Serializable {

    private boolean found;
    private DealSummary deal;
    private String message;

    public GetDealOutput() {
    }

    public GetDealOutput(boolean found, DealSummary deal, String message) {
        this.found = found;
        this.deal = deal;
        this.message = message;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public DealSummary getDeal() {
        return deal;
    }

    public void setDeal(DealSummary deal) {
        this.deal = deal;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}