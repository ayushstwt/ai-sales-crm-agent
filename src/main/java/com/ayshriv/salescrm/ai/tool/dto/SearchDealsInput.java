package com.ayshriv.salescrm.ai.tool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class SearchDealsInput {

    @JsonProperty("title")
    @JsonPropertyDescription("Search keyword matching deal title")
    private String title;

    @JsonProperty("status")
    @JsonPropertyDescription("Filter by deal status: OPEN, WON, LOST")
    private String status;

    @JsonProperty("stageId")
    @JsonPropertyDescription("Filter by pipeline stage ID")
    private Long stageId;

    @JsonProperty("companyId")
    @JsonPropertyDescription("Filter by company ID")
    private Long companyId;

    @JsonProperty("contactId")
    @JsonPropertyDescription("Filter by contact ID")
    private Long contactId;

    @JsonProperty("pageNumber")
    @JsonPropertyDescription("Page number (1-based, default: 1)")
    private Integer pageNumber;

    @JsonProperty("pageSize")
    @JsonPropertyDescription("Page size (default: 10)")
    private Integer pageSize;

    public SearchDealsInput() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
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

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}