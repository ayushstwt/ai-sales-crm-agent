package com.ayshriv.salescrm.activity.dto;

import com.ayshriv.salescrm.activity.entity.ActivityType;
import com.ayshriv.salescrm.common.dto.BaseSearchRequest;

public class ActivitySearchRequest extends BaseSearchRequest {

    private Long organizationId;
    private ActivityType type;
    private Long leadId;
    private Long contactId;
    private Long companyId;
    private Long dealId;

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getDealId() {
        return dealId;
    }

    public void setDealId(Long dealId) {
        this.dealId = dealId;
    }
}