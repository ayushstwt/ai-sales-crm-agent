package com.ayshriv.salescrm.ai.tool.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class DealSummary implements Serializable {

    private Long id;
    private String title;
    private BigDecimal amount;
    private String currency;
    private String status;
    private Long stageId;
    private String stageName;
    private String companyName;
    private String contactName;

    public DealSummary() {
    }

    public DealSummary(Long id, String title, BigDecimal amount, String currency, String status, Long stageId, String stageName, String companyName, String contactName) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.stageId = stageId;
        this.stageName = stageName;
        this.companyName = companyName;
        this.contactName = contactName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
}