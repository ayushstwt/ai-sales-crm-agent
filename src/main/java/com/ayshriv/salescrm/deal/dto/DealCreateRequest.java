package com.ayshriv.salescrm.deal.dto;

import com.ayshriv.salescrm.deal.entity.DealStatus;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DealCreateRequest implements Serializable {

    private Long organizationId;
    private Long companyId;
    private Long contactId;
    private Long pipelineStageId;
    private Long assignedToId;

    @NotBlank(message = "Deal title is required")
    private String title;

    private BigDecimal amount = BigDecimal.ZERO;
    private String currency = "USD";
    private LocalDateTime expectedCloseDate;
    private DealStatus status = DealStatus.OPEN;

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public Long getPipelineStageId() {
        return pipelineStageId;
    }

    public void setPipelineStageId(Long pipelineStageId) {
        this.pipelineStageId = pipelineStageId;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
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

    public LocalDateTime getExpectedCloseDate() {
        return expectedCloseDate;
    }

    public void setExpectedCloseDate(LocalDateTime expectedCloseDate) {
        this.expectedCloseDate = expectedCloseDate;
    }

    public DealStatus getStatus() {
        return status;
    }

    public void setStatus(DealStatus status) {
        this.status = status;
    }
}
