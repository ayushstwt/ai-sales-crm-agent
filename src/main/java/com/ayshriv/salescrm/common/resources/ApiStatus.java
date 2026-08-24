package com.ayshriv.salescrm.common.resources;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

@JsonFilter("apiStatusFilter")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiStatus implements Serializable {

    private String statusType;
    private String text;
    private String token;
    private Long total;
    private com.ayshriv.salescrm.user.entity.User user;
    private java.util.List<com.ayshriv.salescrm.user.entity.User> users;
    private com.ayshriv.salescrm.organization.entity.Organization organization;
    private com.ayshriv.salescrm.user.entity.UserLog log;
    private java.util.List<com.ayshriv.salescrm.user.entity.UserLog> logs;
    private com.ayshriv.salescrm.company.entity.Company company;
    private java.util.List<com.ayshriv.salescrm.company.entity.Company> companies;
    private com.ayshriv.salescrm.contact.entity.Contact contact;
    private java.util.List<com.ayshriv.salescrm.contact.entity.Contact> contacts;
    private com.ayshriv.salescrm.lead.entity.Lead lead;
    private java.util.List<com.ayshriv.salescrm.lead.entity.Lead> leads;
    private com.ayshriv.salescrm.pipeline.entity.Pipeline pipeline;
    private java.util.List<com.ayshriv.salescrm.pipeline.entity.Pipeline> pipelines;
    private com.ayshriv.salescrm.pipeline.entity.PipelineStage pipelineStage;
    private java.util.List<com.ayshriv.salescrm.pipeline.entity.PipelineStage> pipelineStages;
    private com.ayshriv.salescrm.deal.entity.Deal deal;
    private java.util.List<com.ayshriv.salescrm.deal.entity.Deal> deals;
    private com.ayshriv.salescrm.audit.entity.AuditLog auditLog;
    private java.util.List<com.ayshriv.salescrm.audit.entity.AuditLog> auditLogs;

    public ApiStatus() {
    }

    public ApiStatus(String statusType, String text) {
        this.statusType = statusType;
        this.text = text;
    }

    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public com.ayshriv.salescrm.user.entity.User getUser() {
        return user;
    }

    public void setUser(com.ayshriv.salescrm.user.entity.User user) {
        this.user = user;
    }

    public java.util.List<com.ayshriv.salescrm.user.entity.User> getUsers() {
        return users;
    }

    public void setUsers(java.util.List<com.ayshriv.salescrm.user.entity.User> users) {
        this.users = users;
    }

    public com.ayshriv.salescrm.organization.entity.Organization getOrganization() {
        return organization;
    }

    public void setOrganization(com.ayshriv.salescrm.organization.entity.Organization organization) {
        this.organization = organization;
    }

    public com.ayshriv.salescrm.user.entity.UserLog getLog() {
        return log;
    }

    public void setLog(com.ayshriv.salescrm.user.entity.UserLog log) {
        this.log = log;
    }

    public java.util.List<com.ayshriv.salescrm.user.entity.UserLog> getLogs() {
        return logs;
    }

    public void setLogs(java.util.List<com.ayshriv.salescrm.user.entity.UserLog> logs) {
        this.logs = logs;
    }

    public com.ayshriv.salescrm.company.entity.Company getCompany() {
        return company;
    }

    public void setCompany(com.ayshriv.salescrm.company.entity.Company company) {
        this.company = company;
    }

    public java.util.List<com.ayshriv.salescrm.company.entity.Company> getCompanies() {
        return companies;
    }

    public void setCompanies(java.util.List<com.ayshriv.salescrm.company.entity.Company> companies) {
        this.companies = companies;
    }

    public com.ayshriv.salescrm.contact.entity.Contact getContact() {
        return contact;
    }

    public void setContact(com.ayshriv.salescrm.contact.entity.Contact contact) {
        this.contact = contact;
    }

    public java.util.List<com.ayshriv.salescrm.contact.entity.Contact> getContacts() {
        return contacts;
    }

    public void setContacts(java.util.List<com.ayshriv.salescrm.contact.entity.Contact> contacts) {
        this.contacts = contacts;
    }

    public com.ayshriv.salescrm.lead.entity.Lead getLead() {
        return lead;
    }

    public void setLead(com.ayshriv.salescrm.lead.entity.Lead lead) {
        this.lead = lead;
    }

    public java.util.List<com.ayshriv.salescrm.lead.entity.Lead> getLeads() {
        return leads;
    }

    public void setLeads(java.util.List<com.ayshriv.salescrm.lead.entity.Lead> leads) {
        this.leads = leads;
    }

    public com.ayshriv.salescrm.pipeline.entity.Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(com.ayshriv.salescrm.pipeline.entity.Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public java.util.List<com.ayshriv.salescrm.pipeline.entity.Pipeline> getPipelines() {
        return pipelines;
    }

    public void setPipelines(java.util.List<com.ayshriv.salescrm.pipeline.entity.Pipeline> pipelines) {
        this.pipelines = pipelines;
    }

    public com.ayshriv.salescrm.pipeline.entity.PipelineStage getPipelineStage() {
        return pipelineStage;
    }

    public void setPipelineStage(com.ayshriv.salescrm.pipeline.entity.PipelineStage pipelineStage) {
        this.pipelineStage = pipelineStage;
    }

    public java.util.List<com.ayshriv.salescrm.pipeline.entity.PipelineStage> getPipelineStages() {
        return pipelineStages;
    }

    public void setPipelineStages(java.util.List<com.ayshriv.salescrm.pipeline.entity.PipelineStage> pipelineStages) {
        this.pipelineStages = pipelineStages;
    }

    public com.ayshriv.salescrm.deal.entity.Deal getDeal() {
        return deal;
    }

    public void setDeal(com.ayshriv.salescrm.deal.entity.Deal deal) {
        this.deal = deal;
    }

    public java.util.List<com.ayshriv.salescrm.deal.entity.Deal> getDeals() {
        return deals;
    }

    public void setDeals(java.util.List<com.ayshriv.salescrm.deal.entity.Deal> deals) {
        this.deals = deals;
    }

    public com.ayshriv.salescrm.audit.entity.AuditLog getAuditLog() {
        return auditLog;
    }

    public void setAuditLog(com.ayshriv.salescrm.audit.entity.AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    public java.util.List<com.ayshriv.salescrm.audit.entity.AuditLog> getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(java.util.List<com.ayshriv.salescrm.audit.entity.AuditLog> auditLogs) {
        this.auditLogs = auditLogs;
    }
}
