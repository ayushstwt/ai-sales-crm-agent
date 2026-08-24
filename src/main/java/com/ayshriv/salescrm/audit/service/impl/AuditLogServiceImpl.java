package com.ayshriv.salescrm.audit.service.impl;

import com.ayshriv.salescrm.audit.dto.AuditLogSearchRequest;
import com.ayshriv.salescrm.audit.entity.AuditLog;
import com.ayshriv.salescrm.audit.entity.AuditSource;
import com.ayshriv.salescrm.audit.repository.AuditLogRepository;
import com.ayshriv.salescrm.audit.service.AuditLogService;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.resources.LogConstants;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.common.security.TenantContext;
import com.ayshriv.salescrm.common.security.TenantContextService;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final TenantContextService tenantContextService;

    public AuditLogServiceImpl(
            AuditLogRepository auditLogRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            TenantContextService tenantContextService
    ) {
        this.auditLogRepository = auditLogRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.tenantContextService = tenantContextService;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus listAuditLogs(AuditLogSearchRequest request) {
        LOGGER.info("AuditLogService >> listAuditLogs called!");
        try {
            request = (AuditLogSearchRequest) Resources.getDefaultRequest(request);
            TenantContext context = tenantContextService.getCurrentContext();

            Sort.Direction direction = "ASC".equalsIgnoreCase(request.getOrderDir())
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            String orderBy = (request.getOrderBy() != null && !request.getOrderBy().isBlank())
                    ? request.getOrderBy()
                    : "createdOn";

            Pageable pageable = PageRequest.of(
                    Math.max(0, request.getPageNumber() - 1),
                    request.getPageSize(),
                    Sort.by(direction, orderBy)
            );

            Long targetOrgId = request.getOrganizationId();
            if (targetOrgId == null && context != null) {
                targetOrgId = context.getOrganizationId();
            }

            Page<AuditLog> page;
            if (targetOrgId != null) {
                if (request.getResourceType() != null && !request.getResourceType().isBlank()) {
                    page = auditLogRepository.findByOrganizationIdAndResourceTypeAndIsDeletedFalse(targetOrgId, request.getResourceType().trim(), pageable);
                } else if (request.getSource() != null) {
                    page = auditLogRepository.findByOrganizationIdAndSourceAndIsDeletedFalse(targetOrgId, request.getSource(), pageable);
                } else {
                    page = auditLogRepository.findByOrganizationIdAndIsDeletedFalse(targetOrgId, pageable);
                }
            } else {
                page = auditLogRepository.findByIsDeletedFalse(pageable);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.LIST_SUCCESS, LogConstants.AUDIT_LOG);
            status.setAuditLogs(page.getContent());
            status.setTotal(page.getTotalElements());
            return status;

        } catch (Exception e) {
            LOGGER.error("AuditLogService >> listAuditLogs exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus viewAuditLog(Long id) {
        LOGGER.info("AuditLogService >> viewAuditLog called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<AuditLog> auditLogOptional;
            if (context != null && context.getOrganizationId() != null) {
                auditLogOptional = auditLogRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                auditLogOptional = auditLogRepository.findByIdAndIsDeletedFalse(id);
            }

            if (auditLogOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DETAIL_FAILURE, LogConstants.AUDIT_LOG);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.DETAIL_SUCCESS, LogConstants.AUDIT_LOG);
            status.setAuditLog(auditLogOptional.get());
            return status;

        } catch (Exception e) {
            LOGGER.error("AuditLogService >> viewAuditLog exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public AuditLog logAction(Organization organization, User user, String resourceType, Long resourceId, String action, AuditSource source, String details) {
        try {
            if (organization == null || resourceType == null || action == null) {
                LOGGER.warn("AuditLogService >> logAction skipped: organization, resourceType, or action is null");
                return null;
            }

            AuditLog auditLog = new AuditLog();
            auditLog.setOrganization(organization);
            auditLog.setUser(user);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setAction(action);
            auditLog.setSource(source != null ? source : AuditSource.API);
            auditLog.setDetails(details);
            auditLog.setIsActive(true);
            auditLog.setIsDeleted(false);
            auditLog.setCreatedOn(LocalDateTime.now());

            AuditLog savedLog = auditLogRepository.save(auditLog);
            LOGGER.info("AuditLog recorded >> ID: {}, Org: {}, Resource: {}:{}, Action: {}, Source: {}", 
                    savedLog.getId(), organization.getId(), resourceType, resourceId, action, source);
            return savedLog;

        } catch (Exception e) {
            LOGGER.error("AuditLogService >> logAction error: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional
    public AuditLog logAction(String resourceType, Long resourceId, String action, AuditSource source, String details) {
        TenantContext context = tenantContextService.getCurrentContext();
        if (context == null || context.getOrganizationId() == null) {
            LOGGER.warn("AuditLogService >> logAction skipped: no tenant context available");
            return null;
        }

        Organization org = organizationRepository.findByIdAndIsDeletedFalse(context.getOrganizationId()).orElse(null);
        if (org == null) {
            LOGGER.warn("AuditLogService >> logAction skipped: organization not found for ID: {}", context.getOrganizationId());
            return null;
        }

        User user = null;
        if (context.getUserId() != null) {
            user = userRepository.findByIdAndIsDeletedFalse(context.getUserId()).orElse(null);
        }

        return logAction(org, user, resourceType, resourceId, action, source, details);
    }
}
