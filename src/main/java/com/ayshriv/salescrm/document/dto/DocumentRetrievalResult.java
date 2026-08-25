package com.ayshriv.salescrm.document.dto;

import java.util.List;

public class DocumentRetrievalResult {
    private String query;
    private Long organizationId;
    private int totalChunksSearched;
    private List<DocumentChunkDto> matches;

    public DocumentRetrievalResult() {
    }

    public DocumentRetrievalResult(String query, Long organizationId, int totalChunksSearched, List<DocumentChunkDto> matches) {
        this.query = query;
        this.organizationId = organizationId;
        this.totalChunksSearched = totalChunksSearched;
        this.matches = matches;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public int getTotalChunksSearched() {
        return totalChunksSearched;
    }

    public void setTotalChunksSearched(int totalChunksSearched) {
        this.totalChunksSearched = totalChunksSearched;
    }

    public List<DocumentChunkDto> getMatches() {
        return matches;
    }

    public void setMatches(List<DocumentChunkDto> matches) {
        this.matches = matches;
    }
}
