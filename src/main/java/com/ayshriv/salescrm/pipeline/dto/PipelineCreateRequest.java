package com.ayshriv.salescrm.pipeline.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

public class PipelineCreateRequest implements Serializable {

    private Long organizationId;

    @NotBlank(message = "Pipeline name is required")
    private String name;

    private Boolean isDefault = false;
    private List<PipelineStageCreateRequest> stages;

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public List<PipelineStageCreateRequest> getStages() {
        return stages;
    }

    public void setStages(List<PipelineStageCreateRequest> stages) {
        this.stages = stages;
    }
}
