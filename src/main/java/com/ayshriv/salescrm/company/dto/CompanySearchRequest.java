package com.ayshriv.salescrm.company.dto;

import com.ayshriv.salescrm.common.dto.BaseSearchRequest;

public class CompanySearchRequest extends BaseSearchRequest {

    private Long organizationId;
    private String name;
    private String industry;

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

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }
}
