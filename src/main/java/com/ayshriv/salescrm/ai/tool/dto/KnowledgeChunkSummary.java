package com.ayshriv.salescrm.ai.tool.dto;

public class KnowledgeChunkSummary {
    private Long chunkId;
    private Long documentId;
    private String documentTitle;
    private String documentFilename;
    private Integer chunkIndex;
    private String content;
    private Double similarityScore;

    public KnowledgeChunkSummary() {
    }

    public KnowledgeChunkSummary(Long chunkId, Long documentId, String documentTitle, String documentFilename, Integer chunkIndex, String content, Double similarityScore) {
        this.chunkId = chunkId;
        this.documentId = documentId;
        this.documentTitle = documentTitle;
        this.documentFilename = documentFilename;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.similarityScore = similarityScore;
    }

    public Long getChunkId() {
        return chunkId;
    }

    public void setChunkId(Long chunkId) {
        this.chunkId = chunkId;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getDocumentFilename() {
        return documentFilename;
    }

    public void setDocumentFilename(String documentFilename) {
        this.documentFilename = documentFilename;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }
}
