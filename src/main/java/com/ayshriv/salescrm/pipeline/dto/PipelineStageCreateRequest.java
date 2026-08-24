package com.ayshriv.salescrm.pipeline.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public class PipelineStageCreateRequest implements Serializable {

    @NotBlank(message = "Stage name is required")
    private String name;

    private Integer orderIndex = 0;
    private Double probability = 0.0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }
}
