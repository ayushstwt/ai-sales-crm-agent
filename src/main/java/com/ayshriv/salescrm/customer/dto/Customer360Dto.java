package com.ayshriv.salescrm.customer.dto;

import com.ayshriv.salescrm.activity.dto.TimelineItemDto;
import com.ayshriv.salescrm.activity.entity.Activity;
import com.ayshriv.salescrm.company.entity.Company;
import com.ayshriv.salescrm.contact.entity.Contact;
import com.ayshriv.salescrm.deal.entity.Deal;
import com.ayshriv.salescrm.lead.entity.Lead;
import com.ayshriv.salescrm.task.entity.Task;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Customer360Dto implements Serializable {

    private Company company;
    private List<Contact> contacts = new ArrayList<>();
    private List<Lead> leads = new ArrayList<>();
    private List<Deal> deals = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();
    private List<Activity> activities = new ArrayList<>();
    private List<Activity> notes = new ArrayList<>();
    private List<TimelineItemDto> timeline = new ArrayList<>();

    // Key Aggregated Metrics
    private int totalContacts;
    private int totalLeads;
    private int totalDeals;
    private BigDecimal totalPipelineValue = BigDecimal.ZERO;
    private BigDecimal totalWonValue = BigDecimal.ZERO;
    private int openTasksCount;
    private int totalActivitiesCount;

    public Customer360Dto() {
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public List<Lead> getLeads() {
        return leads;
    }

    public void setLeads(List<Lead> leads) {
        this.leads = leads;
    }

    public List<Deal> getDeals() {
        return deals;
    }

    public void setDeals(List<Deal> deals) {
        this.deals = deals;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public List<Activity> getNotes() {
        return notes;
    }

    public void setNotes(List<Activity> notes) {
        this.notes = notes;
    }

    public List<TimelineItemDto> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TimelineItemDto> timeline) {
        this.timeline = timeline;
    }

    public int getTotalContacts() {
        return totalContacts;
    }

    public void setTotalContacts(int totalContacts) {
        this.totalContacts = totalContacts;
    }

    public int getTotalLeads() {
        return totalLeads;
    }

    public void setTotalLeads(int totalLeads) {
        this.totalLeads = totalLeads;
    }

    public int getTotalDeals() {
        return totalDeals;
    }

    public void setTotalDeals(int totalDeals) {
        this.totalDeals = totalDeals;
    }

    public BigDecimal getTotalPipelineValue() {
        return totalPipelineValue;
    }

    public void setTotalPipelineValue(BigDecimal totalPipelineValue) {
        this.totalPipelineValue = totalPipelineValue;
    }

    public BigDecimal getTotalWonValue() {
        return totalWonValue;
    }

    public void setTotalWonValue(BigDecimal totalWonValue) {
        this.totalWonValue = totalWonValue;
    }

    public int getOpenTasksCount() {
        return openTasksCount;
    }

    public void setOpenTasksCount(int openTasksCount) {
        this.openTasksCount = openTasksCount;
    }

    public int getTotalActivitiesCount() {
        return totalActivitiesCount;
    }

    public void setTotalActivitiesCount(int totalActivitiesCount) {
        this.totalActivitiesCount = totalActivitiesCount;
    }
}
