package com.ayshriv.salescrm.common.resources;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.List;

@JsonFilter("apiStatusFilter")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiStatus implements Serializable {

    private String statusType;
    private String text;
    private String token;
    private Long total;
    private com.ayshriv.salescrm.user.entity.User user;
    private List<com.ayshriv.salescrm.user.entity.User> users;
    private com.ayshriv.salescrm.organization.entity.Organization organization;
    private com.ayshriv.salescrm.user.entity.UserLog log;
    private List<com.ayshriv.salescrm.user.entity.UserLog> logs;
    private com.ayshriv.salescrm.company.entity.Company company;
    private List<com.ayshriv.salescrm.company.entity.Company> companies;
    private com.ayshriv.salescrm.contact.entity.Contact contact;
    private List<com.ayshriv.salescrm.contact.entity.Contact> contacts;
    private com.ayshriv.salescrm.lead.entity.Lead lead;
    private List<com.ayshriv.salescrm.lead.entity.Lead> leads;
    private com.ayshriv.salescrm.pipeline.entity.Pipeline pipeline;
    private List<com.ayshriv.salescrm.pipeline.entity.Pipeline> pipelines;
    private com.ayshriv.salescrm.pipeline.entity.PipelineStage pipelineStage;
    private List<com.ayshriv.salescrm.pipeline.entity.PipelineStage> pipelineStages;
    private com.ayshriv.salescrm.deal.entity.Deal deal;
    private List<com.ayshriv.salescrm.deal.entity.Deal> deals;
    private com.ayshriv.salescrm.audit.entity.AuditLog auditLog;
    private List<com.ayshriv.salescrm.audit.entity.AuditLog> auditLogs;
    private com.ayshriv.salescrm.task.entity.Task task;
    private List<com.ayshriv.salescrm.task.entity.Task> tasks;
    private com.ayshriv.salescrm.activity.entity.Activity activity;
    private List<com.ayshriv.salescrm.activity.entity.Activity> activities;
    private com.ayshriv.salescrm.customer.dto.Customer360Dto customer360;
    private com.ayshriv.salescrm.document.entity.Document document;
    private List<com.ayshriv.salescrm.document.entity.Document> documents;

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

    public List<com.ayshriv.salescrm.user.entity.User> getUsers() {
        return users;
    }

    public void setUsers(List<com.ayshriv.salescrm.user.entity.User> users) {
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

    public List<com.ayshriv.salescrm.user.entity.UserLog> getLogs() {
        return logs;
    }

    public void setLogs(List<com.ayshriv.salescrm.user.entity.UserLog> logs) {
        this.logs = logs;
    }

    public com.ayshriv.salescrm.company.entity.Company getCompany() {
        return company;
    }

    public void setCompany(com.ayshriv.salescrm.company.entity.Company company) {
        this.company = company;
    }

    public List<com.ayshriv.salescrm.company.entity.Company> getCompanies() {
        return companies;
    }

    public void setCompanies(List<com.ayshriv.salescrm.company.entity.Company> companies) {
        this.companies = companies;
    }

    public com.ayshriv.salescrm.contact.entity.Contact getContact() {
        return contact;
    }

    public void setContact(com.ayshriv.salescrm.contact.entity.Contact contact) {
        this.contact = contact;
    }

    public List<com.ayshriv.salescrm.contact.entity.Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<com.ayshriv.salescrm.contact.entity.Contact> contacts) {
        this.contacts = contacts;
    }

    public com.ayshriv.salescrm.lead.entity.Lead getLead() {
        return lead;
    }

    public void setLead(com.ayshriv.salescrm.lead.entity.Lead lead) {
        this.lead = lead;
    }

    public List<com.ayshriv.salescrm.lead.entity.Lead> getLeads() {
        return leads;
    }

    public void setLeads(List<com.ayshriv.salescrm.lead.entity.Lead> leads) {
        this.leads = leads;
    }

    public com.ayshriv.salescrm.pipeline.entity.Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(com.ayshriv.salescrm.pipeline.entity.Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public List<com.ayshriv.salescrm.pipeline.entity.Pipeline> getPipelines() {
        return pipelines;
    }

    public void setPipelines(List<com.ayshriv.salescrm.pipeline.entity.Pipeline> pipelines) {
        this.pipelines = pipelines;
    }

    public com.ayshriv.salescrm.pipeline.entity.PipelineStage getPipelineStage() {
        return pipelineStage;
    }

    public void setPipelineStage(com.ayshriv.salescrm.pipeline.entity.PipelineStage pipelineStage) {
        this.pipelineStage = pipelineStage;
    }

    public List<com.ayshriv.salescrm.pipeline.entity.PipelineStage> getPipelineStages() {
        return pipelineStages;
    }

    public void setPipelineStages(List<com.ayshriv.salescrm.pipeline.entity.PipelineStage> pipelineStages) {
        this.pipelineStages = pipelineStages;
    }

    public com.ayshriv.salescrm.deal.entity.Deal getDeal() {
        return deal;
    }

    public void setDeal(com.ayshriv.salescrm.deal.entity.Deal deal) {
        this.deal = deal;
    }

    public List<com.ayshriv.salescrm.deal.entity.Deal> getDeals() {
        return deals;
    }

    public void setDeals(List<com.ayshriv.salescrm.deal.entity.Deal> deals) {
        this.deals = deals;
    }

    public com.ayshriv.salescrm.audit.entity.AuditLog getAuditLog() {
        return auditLog;
    }

    public void setAuditLog(com.ayshriv.salescrm.audit.entity.AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    public List<com.ayshriv.salescrm.audit.entity.AuditLog> getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(List<com.ayshriv.salescrm.audit.entity.AuditLog> auditLogs) {
        this.auditLogs = auditLogs;
    }

    public com.ayshriv.salescrm.task.entity.Task getTask() {
        return task;
    }

    public void setTask(com.ayshriv.salescrm.task.entity.Task task) {
        this.task = task;
    }

    public List<com.ayshriv.salescrm.task.entity.Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<com.ayshriv.salescrm.task.entity.Task> tasks) {
        this.tasks = tasks;
    }

    public com.ayshriv.salescrm.activity.entity.Activity getActivity() {
        return activity;
    }

    public void setActivity(com.ayshriv.salescrm.activity.entity.Activity activity) {
        this.activity = activity;
    }

    public List<com.ayshriv.salescrm.activity.entity.Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<com.ayshriv.salescrm.activity.entity.Activity> activities) {
        this.activities = activities;
    }

    public com.ayshriv.salescrm.customer.dto.Customer360Dto getCustomer360() {
        return customer360;
    }

    public void setCustomer360(com.ayshriv.salescrm.customer.dto.Customer360Dto customer360) {
        this.customer360 = customer360;
    }

    public com.ayshriv.salescrm.document.entity.Document getDocument() {
        return document;
    }

    public void setDocument(com.ayshriv.salescrm.document.entity.Document document) {
        this.document = document;
    }

    public List<com.ayshriv.salescrm.document.entity.Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<com.ayshriv.salescrm.document.entity.Document> documents) {
        this.documents = documents;
    }
}