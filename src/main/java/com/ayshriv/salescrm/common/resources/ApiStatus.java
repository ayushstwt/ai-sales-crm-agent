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
}
