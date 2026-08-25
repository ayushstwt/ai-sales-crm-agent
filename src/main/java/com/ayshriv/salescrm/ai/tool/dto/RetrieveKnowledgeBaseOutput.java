package com.ayshriv.salescrm.ai.tool.dto;

import java.util.List;

public class RetrieveKnowledgeBaseOutput {
    private boolean success;
    private String query;
    private int totalMatches;
    private List<KnowledgeChunkSummary> chunks;
    private String message;

    public RetrieveKnowledgeBaseOutput() {
    }

    public RetrieveKnowledgeBaseOutput(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public RetrieveKnowledgeBaseOutput(boolean success, String query, int totalMatches, List<KnowledgeChunkSummary> chunks, String message) {
        this.success = success;
        this.query = query;
        this.totalMatches = totalMatches;
        this.chunks = chunks;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getTotalMatches() {
        return totalMatches;
    }

    public void setTotalMatches(int totalMatches) {
        this.totalMatches = totalMatches;
    }

    public List<KnowledgeChunkSummary> getChunks() {
        return chunks;
    }

    public void setChunks(List<KnowledgeChunkSummary> chunks) {
        this.chunks = chunks;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
