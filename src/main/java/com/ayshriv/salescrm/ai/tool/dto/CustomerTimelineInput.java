package com.ayshriv.salescrm.ai.tool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class CustomerTimelineInput {

    @JsonProperty("companyId")
    @JsonPropertyDescription("Company ID to view activity timeline for (optional)")
    private Long companyId;

    @JsonProperty("contactId")
    @JsonPropertyDescription("Contact ID to view activity timeline for (optional)")
    private Long contactId;

    @JsonProperty("leadId")
    @JsonPropertyDescription("Lead ID to view activity timeline for (optional)")
    private Long leadId;

    @JsonProperty("dealId")
    @JsonPropertyDescription("Deal ID to view activity timeline for (optional)")
    private Long dealId;

    public CustomerTimelineInput() {
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public Long getDealId() {
        return dealId;
    }

    public void setDealId(Long dealId) {
        this.dealId = dealId;
    }
}