package com.ayshriv.salescrm.document.dto;

public class DocumentChunkDto {
    private Long id;
    private Long documentId;
    private String documentTitle;
    private String documentFilename;
    private Integer chunkIndex;
    private String content;
    private Integer tokenCount;
    private Double similarityScore;

    public DocumentChunkDto() {
    }

    public DocumentChunkDto(Long id, Long documentId, String documentTitle, String documentFilename, Integer chunkIndex, String content, Integer tokenCount, Double similarityScore) {
        this.id = id;
        this.documentId = documentId;
        this.documentTitle = documentTitle;
        this.documentFilename = documentFilename;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.tokenCount = tokenCount;
        this.similarityScore = similarityScore;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }
}
