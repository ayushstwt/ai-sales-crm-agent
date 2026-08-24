package com.ayshriv.salescrm.lead.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LeadConvertRequest implements Serializable {

    private Long companyId;
    private String companyName;

    // Contact details override (optional)
    private String contactJobTitle;
    private String contactDepartment;

    // Optional Deal creation
    private Boolean createDeal = false;
    private String dealTitle;
    private BigDecimal dealAmount;
    private Long pipelineStageId;
    private LocalDateTime expectedCloseDate;

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

    public String getContactJobTitle() {
        return contactJobTitle;
    }

    public void setContactJobTitle(String contactJobTitle) {
        this.contactJobTitle = contactJobTitle;
    }

    public String getContactDepartment() {
        return contactDepartment;
    }

    public void setContactDepartment(String contactDepartment) {
        this.contactDepartment = contactDepartment;
    }

    public Boolean getCreateDeal() {
        return createDeal;
    }

    public void setCreateDeal(Boolean createDeal) {
        this.createDeal = createDeal;
    }

    public String getDealTitle() {
        return dealTitle;
    }

    public void setDealTitle(String dealTitle) {
        this.dealTitle = dealTitle;
    }

    public BigDecimal getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(BigDecimal dealAmount) {
        this.dealAmount = dealAmount;
    }

    public Long getPipelineStageId() {
        return pipelineStageId;
    }

    public void setPipelineStageId(Long pipelineStageId) {
        this.pipelineStageId = pipelineStageId;
    }

    public LocalDateTime getExpectedCloseDate() {
        return expectedCloseDate;
    }

    public void setExpectedCloseDate(LocalDateTime expectedCloseDate) {
        this.expectedCloseDate = expectedCloseDate;
    }
}
