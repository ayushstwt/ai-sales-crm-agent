package com.ayshriv.salescrm.ai.tool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class GetLeadInput {

    @com.fasterxml.jackson.annotation.JsonAlias({"leadId", "id"})
    @JsonProperty("id")
    @JsonPropertyDescription("The ID of the lead to retrieve")
    private Long id;

    public GetLeadInput() {
    }

    public GetLeadInput(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}