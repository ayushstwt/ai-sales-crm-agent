package com.ayshriv.salescrm.customer.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Customer360SummaryDto implements Serializable {

    private Long companyId;
    private String companyName;
    private String domain;
    private String industry;
    private String summary;
    private String relationshipHealth;
    private String keyHighlights;
    private String nextRecommendedActions;
    private Customer360Dto aggregation;
    private LocalDateTime generatedAt;

    public Customer360SummaryDto() {
    }

    public Customer360SummaryDto(Long companyId, String companyName, String summary, Customer360Dto aggregation) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.summary = summary;
        this.aggregation = aggregation;
        this.generatedAt = LocalDateTime.now();
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRelationshipHealth() {
        return relationshipHealth;
    }

    public void setRelationshipHealth(String relationshipHealth) {
        this.relationshipHealth = relationshipHealth;
    }

    public String getKeyHighlights() {
        return keyHighlights;
    }

    public void setKeyHighlights(String keyHighlights) {
        this.keyHighlights = keyHighlights;
    }

    public String getNextRecommendedActions() {
        return nextRecommendedActions;
    }

    public void setNextRecommendedActions(String nextRecommendedActions) {
        this.nextRecommendedActions = nextRecommendedActions;
    }

    public Customer360Dto getAggregation() {
        return aggregation;
    }

    public void setAggregation(Customer360Dto aggregation) {
        this.aggregation = aggregation;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
