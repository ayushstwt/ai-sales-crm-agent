package com.ayshriv.salescrm.ai.tool.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchLeadsInput {

    @JsonProperty("name")
    @JsonPropertyDescription("Search keyword matching lead first name or last name")
    private String name;

    @JsonProperty("email")
    @JsonPropertyDescription("Search keyword matching lead email address")
    private String email;

    @JsonProperty("companyName")
    @JsonPropertyDescription("Search keyword matching lead company name")
    private String companyName;

    @JsonProperty("status")
    @JsonPropertyDescription("Filter by lead status: NEW, CONTACTED, QUALIFIED, UNQUALIFIED, CONVERTED, LOST")
    private String status;

    @JsonProperty("pageNumber")
    @JsonPropertyDescription("Page number (1-based, default: 1)")
    private Integer pageNumber;

    @JsonProperty("pageSize")
    @JsonPropertyDescription("Page size (default: 10)")
    private Integer pageSize;

    public SearchLeadsInput() {
    }

    public SearchLeadsInput(String name, String email, String companyName, String status) {
        this.name = name;
        this.email = email;
        this.companyName = companyName;
        this.status = status;
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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