package com.ayshriv.salescrm.audit.service;

import com.ayshriv.salescrm.audit.dto.AuditLogSearchRequest;
import com.ayshriv.salescrm.audit.entity.AuditLog;
import com.ayshriv.salescrm.audit.entity.AuditSource;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.user.entity.User;

public interface AuditLogService {

    ApiStatus listAuditLogs(AuditLogSearchRequest request);

    ApiStatus viewAuditLog(Long id);

    AuditLog logAction(Organization organization, User user, String resourceType, Long resourceId, String action, AuditSource source, String details);

    AuditLog logAction(String resourceType, Long resourceId, String action, AuditSource source, String details);
}
