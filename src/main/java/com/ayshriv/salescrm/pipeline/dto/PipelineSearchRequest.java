package com.ayshriv.salescrm.pipeline.dto;

import com.ayshriv.salescrm.common.dto.BaseSearchRequest;

public class PipelineSearchRequest extends BaseSearchRequest {

    private Long organizationId;
    private String name;

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
}
