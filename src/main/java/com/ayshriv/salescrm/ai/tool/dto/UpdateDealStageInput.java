package com.ayshriv.salescrm.ai.tool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class UpdateDealStageInput {

    @JsonProperty("dealId")
    @JsonPropertyDescription("The ID of the deal to update")
    private Long dealId;

    @JsonProperty("stageId")
    @JsonPropertyDescription("The target pipeline stage ID to move the deal into")
    private Long stageId;

    @JsonProperty("status")
    @JsonPropertyDescription("Optional deal status override (OPEN, WON, LOST)")
    private String status;

    public UpdateDealStageInput() {
    }

    public UpdateDealStageInput(Long dealId, Long stageId) {
        this.dealId = dealId;
        this.stageId = stageId;
    }

    public Long getDealId() {
        return dealId;
    }

    public void setDealId(Long dealId) {
        this.dealId = dealId;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}