package com.ayshriv.salescrm.ai.tool.dto;

import java.io.Serializable;

public class UpdateDealStageOutput implements Serializable {

    private boolean success;
    private Long dealId;
    private String stageName;
    private String status;
    private String message;

    public UpdateDealStageOutput() {
    }

    public UpdateDealStageOutput(boolean success, Long dealId, String stageName, String status, String message) {
        this.success = success;
        this.dealId = dealId;
        this.stageName = stageName;
        this.status = status;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Long getDealId() {
        return dealId;
    }

    public void setDealId(Long dealId) {
        this.dealId = dealId;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}