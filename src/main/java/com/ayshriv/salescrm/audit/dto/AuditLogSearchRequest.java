package com.ayshriv.salescrm.audit.dto;

import com.ayshriv.salescrm.audit.entity.AuditSource;
import com.ayshriv.salescrm.common.dto.BaseSearchRequest;

public class AuditLogSearchRequest extends BaseSearchRequest {

    private Long organizationId;
    private Long userId;
    private String resourceType;
    private Long resourceId;
    private String action;
    private AuditSource source;

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public AuditSource getSource() {
        return source;
    }

    public void setSource(AuditSource source) {
        this.source = source;
    }
}
