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
}
