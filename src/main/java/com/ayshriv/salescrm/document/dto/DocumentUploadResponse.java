package com.ayshriv.salescrm.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentUploadResponse {
    private Long documentId;
    private String filename;
    private String fileType;
    private Long fileSize;
    private String title;
    private Integer chunkCount;
    private String extractedText;
    private List<DocumentChunkDto> chunks;
    private String message;

    public DocumentUploadResponse() {
    }

    public DocumentUploadResponse(Long documentId, String filename, String fileType, Long fileSize, String title, Integer chunkCount, String extractedText, String message) {
        this.documentId = documentId;
        this.filename = filename;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.title = title;
        this.chunkCount = chunkCount;
        this.extractedText = extractedText;
        this.message = message;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public List<DocumentChunkDto> getChunks() {
        return chunks;
    }

    public void setChunks(List<DocumentChunkDto> chunks) {
        this.chunks = chunks;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
