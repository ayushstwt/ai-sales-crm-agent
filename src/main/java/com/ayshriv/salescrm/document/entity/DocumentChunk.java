package com.ayshriv.salescrm.document.entity;

import com.ayshriv.salescrm.common.entity.BaseEntity;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk extends BaseEntity {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "token_count", nullable = false)
    private Integer tokenCount = 0;

    @Column(name = "embedding", columnDefinition = "TEXT")
    private String embedding;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    @Transient
    private Double similarityScore;

    public DocumentChunk() {
    }

    public DocumentChunk(Organization organization, Document document, Integer chunkIndex, String content, Integer tokenCount) {
        this.organization = organization;
        this.document = document;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.tokenCount = tokenCount != null ? tokenCount : 0;
    }

    @PrePersist
    protected void onCreate() {
        if (createdOn == null) {
            createdOn = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (tokenCount == null) {
            tokenCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedOn = LocalDateTime.now();
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
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

    public String getEmbedding() {
        return embedding;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }

    public void setEmbeddingVector(List<Double> vector) {
        if (vector == null || vector.isEmpty()) {
            this.embedding = null;
            return;
        }
        try {
            this.embedding = MAPPER.writeValueAsString(vector);
        } catch (Exception e) {
            this.embedding = null;
        }
    }

    public List<Double> getEmbeddingVector() {
        if (this.embedding == null || this.embedding.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(this.embedding, new TypeReference<List<Double>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean deleted) {
        isDeleted = deleted;
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

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }
}
