package com.ayshriv.salescrm.document.dto;

import java.time.LocalDateTime;

public class DocumentDto {
    private Long id;
    private Long organizationId;
    private String filename;
    private String fileType;
    private Long fileSize;
    private String title;
    private Integer chunkCount;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;

    public DocumentDto() {
    }

    public DocumentDto(Long id, Long organizationId, String filename, String fileType, Long fileSize, String title, Integer chunkCount, LocalDateTime createdOn, LocalDateTime updatedOn) {
        this.id = id;
        this.organizationId = organizationId;
        this.filename = filename;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.title = title;
        this.chunkCount = chunkCount;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
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

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }
}
