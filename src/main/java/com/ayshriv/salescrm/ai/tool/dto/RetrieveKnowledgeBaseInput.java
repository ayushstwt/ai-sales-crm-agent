package com.ayshriv.salescrm.ai.tool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class RetrieveKnowledgeBaseInput {

    @JsonProperty(required = true)
    @JsonPropertyDescription("The natural language search query, topic, question, or key terms to look up in the organization's uploaded knowledge base and documents.")
    private String query;

    @JsonProperty
    @JsonPropertyDescription("Maximum number of relevant document chunks to return (default is 4).")
    private Integer topK = 4;

    public RetrieveKnowledgeBaseInput() {
    }

    public RetrieveKnowledgeBaseInput(String query, Integer topK) {
        this.query = query;
        this.topK = topK != null ? topK : 4;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }
}
