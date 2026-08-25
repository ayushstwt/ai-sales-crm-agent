package com.ayshriv.salescrm.customer.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class GetCustomer360Output implements Serializable {

    private boolean success;
    private Long companyId;
    private String companyName;
    private String domain;
    private String industry;
    private int contactCount;
    private int dealCount;
    private BigDecimal totalPipelineValue;
    private BigDecimal wonValue;
    private int openTasksCount;
    private int activityCount;
    private List<String> contactNames;
    private List<String> dealSummaries;
    private List<String> recentActivitySummaries;
    private List<String> notes;
    private String message;

    public GetCustomer360Output() {
    }

    public GetCustomer360Output(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    public int getContactCount() {
        return contactCount;
    }

    public void setContactCount(int contactCount) {
        this.contactCount = contactCount;
    }

    public int getDealCount() {
        return dealCount;
    }

    public void setDealCount(int dealCount) {
        this.dealCount = dealCount;
    }

    public BigDecimal getTotalPipelineValue() {
        return totalPipelineValue;
    }

    public void setTotalPipelineValue(BigDecimal totalPipelineValue) {
        this.totalPipelineValue = totalPipelineValue;
    }

    public BigDecimal getWonValue() {
        return wonValue;
    }

    public void setWonValue(BigDecimal wonValue) {
        this.wonValue = wonValue;
    }

    public int getOpenTasksCount() {
        return openTasksCount;
    }

    public void setOpenTasksCount(int openTasksCount) {
        this.openTasksCount = openTasksCount;
    }

    public int getActivityCount() {
        return activityCount;
    }

    public void setActivityCount(int activityCount) {
        this.activityCount = activityCount;
    }

    public List<String> getContactNames() {
        return contactNames;
    }

    public void setContactNames(List<String> contactNames) {
        this.contactNames = contactNames;
    }

    public List<String> getDealSummaries() {
        return dealSummaries;
    }

    public void setDealSummaries(List<String> dealSummaries) {
        this.dealSummaries = dealSummaries;
    }

    public List<String> getRecentActivitySummaries() {
        return recentActivitySummaries;
    }

    public void setRecentActivitySummaries(List<String> recentActivitySummaries) {
        this.recentActivitySummaries = recentActivitySummaries;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
