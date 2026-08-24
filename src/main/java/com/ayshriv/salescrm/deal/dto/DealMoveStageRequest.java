package com.ayshriv.salescrm.deal.dto;

import com.ayshriv.salescrm.deal.entity.DealStatus;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class DealMoveStageRequest implements Serializable {

    @NotNull(message = "Pipeline stage ID is required")
    private Long pipelineStageId;

    private DealStatus status;

    public Long getPipelineStageId() {
        return pipelineStageId;
    }

    public void setPipelineStageId(Long pipelineStageId) {
        this.pipelineStageId = pipelineStageId;
    }

    public DealStatus getStatus() {
        return status;
    }

    public void setStatus(DealStatus status) {
        this.status = status;
    }
}
