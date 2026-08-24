package com.ayshriv.salescrm.activity.service.impl;

import com.ayshriv.salescrm.activity.dto.ActivityCreateRequest;
import com.ayshriv.salescrm.activity.dto.ActivitySearchRequest;
import com.ayshriv.salescrm.activity.dto.TimelineItemDto;
import com.ayshriv.salescrm.activity.entity.Activity;
import com.ayshriv.salescrm.activity.repository.ActivityRepository;
import com.ayshriv.salescrm.activity.service.ActivityService;
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
import com.ayshriv.salescrm.company.repository.CompanyRepository;
import com.ayshriv.salescrm.contact.repository.ContactRepository;
import com.ayshriv.salescrm.deal.repository.DealRepository;
import com.ayshriv.salescrm.lead.repository.LeadRepository;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.repository.UserRepository;
import com.ayshriv.salescrm.user.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ActivityServiceImpl implements ActivityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityServiceImpl.class);

    private final ActivityRepository activityRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final ContactRepository contactRepository;
    private final CompanyRepository companyRepository;
    private final DealRepository dealRepository;
    private final AuditLogRepository auditLogRepository;
    private final TenantContextService tenantContextService;
    private final LogService logService;
    private final AuditLogService auditLogService;

    public ActivityServiceImpl(
            ActivityRepository activityRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            LeadRepository leadRepository,
            ContactRepository contactRepository,
            CompanyRepository companyRepository,
            DealRepository dealRepository,
            AuditLogRepository auditLogRepository,
            TenantContextService tenantContextService,
            LogService logService,
            AuditLogService auditLogService
    ) {
        this.activityRepository = activityRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.leadRepository = leadRepository;
        this.contactRepository = contactRepository;
        this.companyRepository = companyRepository;
        this.dealRepository = dealRepository;
        this.auditLogRepository = auditLogRepository;
        this.tenantContextService = tenantContextService;
        this.logService = logService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus listActivities(ActivitySearchRequest request) {
        LOGGER.info("ActivityService >> listActivities called!");
        try {
            request = (ActivitySearchRequest) Resources.getDefaultRequest(request);
            TenantContext context = tenantContextService.getCurrentContext();

            Sort.Direction direction = "DESC".equalsIgnoreCase(request.getOrderDir())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            String orderBy = (request.getOrderBy() != null && !request.getOrderBy().isBlank())
                    ? request.getOrderBy()
                    : "activityDate";

            Pageable pageable = PageRequest.of(
                    Math.max(0, request.getPageNumber() - 1),
                    request.getPageSize(),
                    Sort.by(direction, orderBy)
            );

            Long targetOrgId = request.getOrganizationId();
            if (targetOrgId == null && context != null) {
                targetOrgId = context.getOrganizationId();
            }

            Page<Activity> page;
            if (targetOrgId != null) {
                page = activityRepository.findByOrganizationIdAndIsDeletedFalse(targetOrgId, pageable);
            } else {
                page = activityRepository.findByIsDeletedFalse(pageable);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.LIST_SUCCESS, LogConstants.ACTIVITY);
            status.setActivities(page.getContent());
            status.setTotal(page.getTotalElements());
            return status;

        } catch (Exception e) {
            LOGGER.error("ActivityService >> listActivities exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus viewActivity(Long id) {
        LOGGER.info("ActivityService >> viewActivity called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Activity> activityOptional;
            if (context != null && context.getOrganizationId() != null) {
                activityOptional = activityRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                activityOptional = activityRepository.findByIdAndIsDeletedFalse(id);
            }

            if (activityOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DETAIL_FAILURE, LogConstants.ACTIVITY);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.DETAIL_SUCCESS, LogConstants.ACTIVITY);
            status.setActivity(activityOptional.get());
            return status;

        } catch (Exception e) {
            LOGGER.error("ActivityService >> viewActivity exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus createActivity(ActivityCreateRequest request) {
        LOGGER.info("ActivityService >> createActivity called for title: {}", request != null ? request.getTitle() : null);
        try {
            if (request == null || request.getType() == null || request.getTitle() == null || request.getTitle().isBlank()) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "type or title", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Long targetOrgId = request.getOrganizationId();
            if (targetOrgId == null && context != null) {
                targetOrgId = context.getOrganizationId();
            }

            if (targetOrgId == null) {
                return Resources.setStatus(Constants.FAILURE, "Organization ID is required.", null);
            }

            Optional<Organization> orgOptional = organizationRepository.findByIdAndIsDeletedFalse(targetOrgId);
            if (orgOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, "Organization not found.", null);
            }

            Organization organization = orgOptional.get();
            User currentUser = resolveCurrentUser(context);

            Activity activity = new Activity();
            activity.setOrganization(organization);
            activity.setUser(currentUser);
            activity.setType(request.getType());
            activity.setTitle(request.getTitle().trim());
            activity.setDescription(request.getDescription());
            activity.setActivityDate(request.getActivityDate() != null ? request.getActivityDate() : LocalDateTime.now());
            activity.setIsActive(true);
            activity.setIsDeleted(false);
            activity.setCreatedOn(LocalDateTime.now());

            if (request.getLeadId() != null) {
                leadRepository.findByIdAndIsDeletedFalse(request.getLeadId()).ifPresent(activity::setLead);
            }
            if (request.getContactId() != null) {
                contactRepository.findByIdAndIsDeletedFalse(request.getContactId()).ifPresent(activity::setContact);
            }
            if (request.getCompanyId() != null) {
                companyRepository.findByIdAndIsDeletedFalse(request.getCompanyId()).ifPresent(activity::setCompany);
            }
            if (request.getDealId() != null) {
                dealRepository.findByIdAndIsDeletedFalse(request.getDealId()).ifPresent(activity::setDeal);
            }

            Activity savedActivity = activityRepository.save(activity);

            logService.createLog(currentUser, LogConstants.ACTIVITY, LogConstants.ADD, LocalDateTime.now(), null);
            auditLogService.logAction(
                    organization,
                    currentUser,
                    LogConstants.ACTIVITY,
                    savedActivity.getId(),
                    LogConstants.ADD,
                    AuditSource.API,
                    "Recorded activity: " + savedActivity.getTitle() + " (" + savedActivity.getType() + ")"
            );

            ApiStatus status = Resources.setStatus(Constants.CREATED, Constants.SAVE_SUCCESS, LogConstants.ACTIVITY);
            status.setActivity(savedActivity);
            return status;

        } catch (Exception e) {
            LOGGER.error("ActivityService >> createActivity exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus deleteActivity(Long id) {
        LOGGER.info("ActivityService >> deleteActivity called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Activity> activityOptional;
            if (context != null && context.getOrganizationId() != null) {
                activityOptional = activityRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                activityOptional = activityRepository.findByIdAndIsDeletedFalse(id);
            }

            if (activityOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DELETE_FAILURE, LogConstants.ACTIVITY);
            }

            Activity activity = activityOptional.get();
            activity.setIsDeleted(true);
            activity.setUpdatedOn(LocalDateTime.now());
            activityRepository.save(activity);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.ACTIVITY, LogConstants.DELETE, LocalDateTime.now(), null);
            auditLogService.logAction(
                    activity.getOrganization(),
                    currentUser,
                    LogConstants.ACTIVITY,
                    activity.getId(),
                    LogConstants.DELETE,
                    AuditSource.API,
                    "Soft-deleted activity: " + activity.getTitle()
            );

            return Resources.setStatus(Constants.SUCCESS, Constants.DELETE_SUCCESS, LogConstants.ACTIVITY);

        } catch (Exception e) {
            LOGGER.error("ActivityService >> deleteActivity exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimelineItemDto> getCustomerTimeline(Long leadId, Long contactId, Long companyId, Long dealId) {
        LOGGER.info("ActivityService >> getCustomerTimeline called for lead: {}, contact: {}, company: {}, deal: {}",
                leadId, contactId, companyId, dealId);

        TenantContext context = tenantContextService.getCurrentContext();
        Long orgId = context != null ? context.getOrganizationId() : null;

        List<TimelineItemDto> timeline = new ArrayList<>();

        if (orgId != null) {
            List<Activity> activities = new ArrayList<>();
            if (companyId != null) {
                activities.addAll(activityRepository.findByOrganizationIdAndCompanyIdAndIsDeletedFalseOrderByActivityDateDesc(orgId, companyId));
            }
            if (contactId != null) {
                activities.addAll(activityRepository.findByOrganizationIdAndContactIdAndIsDeletedFalseOrderByActivityDateDesc(orgId, contactId));
            }
            if (leadId != null) {
                activities.addAll(activityRepository.findByOrganizationIdAndLeadIdAndIsDeletedFalseOrderByActivityDateDesc(orgId, leadId));
            }
            if (dealId != null) {
                activities.addAll(activityRepository.findByOrganizationIdAndDealIdAndIsDeletedFalseOrderByActivityDateDesc(orgId, dealId));
            }

            for (Activity a : activities) {
                timeline.add(new TimelineItemDto(
                        a.getId(),
                        a.getType().name(),
                        a.getTitle(),
                        a.getDescription(),
                        "ACTIVITY",
                        a.getUser() != null ? a.getUser().getFirstName() + " " + (a.getUser().getLastName() != null ? a.getUser().getLastName() : "") : "System",
                        a.getActivityDate() != null ? a.getActivityDate() : a.getCreatedOn()
                ));
            }

            // Also aggregate audit logs for this resource if available
            String resourceType = null;
            Long resourceId = null;
            if (companyId != null) {
                resourceType = LogConstants.COMPANY;
                resourceId = companyId;
            } else if (dealId != null) {
                resourceType = LogConstants.DEAL;
                resourceId = dealId;
            } else if (leadId != null) {
                resourceType = LogConstants.LEAD;
                resourceId = leadId;
            } else if (contactId != null) {
                resourceType = LogConstants.CONTACT;
                resourceId = contactId;
            }

            if (resourceType != null && resourceId != null) {
                List<AuditLog> auditLogs = auditLogRepository.findByOrganizationIdAndResourceTypeAndIsDeletedFalse(
                        orgId, resourceType, PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdOn"))
                ).getContent();

                for (AuditLog log : auditLogs) {
                    if (resourceId.equals(log.getResourceId())) {
                        timeline.add(new TimelineItemDto(
                                log.getId(),
                                log.getAction(),
                                log.getResourceType() + " " + log.getAction(),
                                log.getDetails(),
                                log.getSource() != null ? log.getSource().name() : "API",
                                log.getUser() != null ? log.getUser().getFirstName() : "System",
                                log.getCreatedOn()
                        ));
                    }
                }
            }
        }

        timeline.sort(Comparator.comparing(TimelineItemDto::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())));
        return timeline;
    }

    private User resolveCurrentUser(TenantContext context) {
        if (context != null && context.getUserId() != null) {
            return userRepository.findById(context.getUserId()).orElse(null);
        }
        return null;
    }
}