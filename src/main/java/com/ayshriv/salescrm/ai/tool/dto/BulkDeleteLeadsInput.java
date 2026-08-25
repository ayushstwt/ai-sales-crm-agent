package com.ayshriv.salescrm.ai.tool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public class BulkDeleteLeadsInput {

    @JsonPropertyDescription("Optional lead status to filter by (e.g., NEW, CONTACTED, QUALIFIED, UNQUALIFIED, CONVERTED, LOST)")
    private String status;

    @JsonPropertyDescription("Optional company name to filter leads by")
    private String companyName;

    @JsonPropertyDescription("Optional lead name to filter by")
    private String name;

    @JsonPropertyDescription("Optional lead email to filter by")
    private String email;

    @JsonPropertyDescription("Optional specific list of lead IDs to delete")
    private List<Long> leadIds;

    public BulkDeleteLeadsInput() {
    }

    public BulkDeleteLeadsInput(String status, String companyName, String name, String email, List<Long> leadIds) {
        this.status = status;
        this.companyName = companyName;
        this.name = name;
        this.email = email;
        this.leadIds = leadIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Long> getLeadIds() {
        return leadIds;
    }

    public void setLeadIds(List<Long> leadIds) {
        this.leadIds = leadIds;
    }
}
