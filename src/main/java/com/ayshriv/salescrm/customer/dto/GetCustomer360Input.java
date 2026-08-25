package com.ayshriv.salescrm.customer.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class GetCustomer360Input {

    @JsonPropertyDescription("The ID of the company/customer to aggregate 360-degree data for")
    private Long customerId;

    @JsonPropertyDescription("Optional company name to search and aggregate if ID is not directly known")
    private String companyName;

    public GetCustomer360Input() {
    }

    public GetCustomer360Input(Long customerId, String companyName) {
        this.customerId = customerId;
        this.companyName = companyName;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
