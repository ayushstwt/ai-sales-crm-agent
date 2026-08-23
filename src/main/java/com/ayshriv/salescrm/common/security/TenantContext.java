package com.ayshriv.salescrm.common.security;

import java.io.Serializable;

public class TenantContext implements Serializable {

    private Long userId;
    private Long organizationId;
    private String role;

    public TenantContext() {
    }

    public TenantContext(Long userId, Long organizationId, String role) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
