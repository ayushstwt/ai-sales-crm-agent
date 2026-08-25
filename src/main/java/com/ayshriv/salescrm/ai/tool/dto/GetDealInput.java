package com.ayshriv.salescrm.ai.tool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class GetDealInput {

    @com.fasterxml.jackson.annotation.JsonAlias({"dealId", "id"})
    @JsonProperty("id")
    @JsonPropertyDescription("The ID of the deal to retrieve")
    private Long id;

    public GetDealInput() {
    }

    public GetDealInput(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}