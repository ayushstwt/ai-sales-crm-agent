package com.ayshriv.salescrm.deal.service.impl;

import com.ayshriv.salescrm.audit.entity.AuditSource;
import com.ayshriv.salescrm.audit.service.AuditLogService;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.resources.LogConstants;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.common.security.TenantContext;
import com.ayshriv.salescrm.common.security.TenantContextService;
import com.ayshriv.salescrm.company.entity.Company;
import com.ayshriv.salescrm.company.repository.CompanyRepository;
import com.ayshriv.salescrm.contact.entity.Contact;
import com.ayshriv.salescrm.contact.repository.ContactRepository;
import com.ayshriv.salescrm.deal.dto.DealCreateRequest;
import com.ayshriv.salescrm.deal.dto.DealMoveStageRequest;
import com.ayshriv.salescrm.deal.dto.DealSearchRequest;
import com.ayshriv.salescrm.deal.dto.DealUpdateRequest;
import com.ayshriv.salescrm.deal.entity.Deal;
import com.ayshriv.salescrm.deal.entity.DealStatus;
import com.ayshriv.salescrm.deal.repository.DealRepository;
import com.ayshriv.salescrm.deal.service.DealService;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.pipeline.entity.PipelineStage;
import com.ayshriv.salescrm.pipeline.repository.PipelineStageRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DealServiceImpl implements DealService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DealServiceImpl.class);

    private final DealRepository dealRepository;
    private final OrganizationRepository organizationRepository;
    private final CompanyRepository companyRepository;
    private final ContactRepository contactRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final UserRepository userRepository;
    private final TenantContextService tenantContextService;
    private final LogService logService;
    private final AuditLogService auditLogService;

    public DealServiceImpl(
            DealRepository dealRepository,
            OrganizationRepository organizationRepository,
            CompanyRepository companyRepository,
            ContactRepository contactRepository,
            PipelineStageRepository pipelineStageRepository,
            UserRepository userRepository,
            TenantContextService tenantContextService,
            LogService logService,
            AuditLogService auditLogService
    ) {
        this.dealRepository = dealRepository;
        this.organizationRepository = organizationRepository;
        this.companyRepository = companyRepository;
        this.contactRepository = contactRepository;
        this.pipelineStageRepository = pipelineStageRepository;
        this.userRepository = userRepository;
        this.tenantContextService = tenantContextService;
        this.logService = logService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus listDeals(DealSearchRequest request) {
        LOGGER.info("DealService >> listDeals called!");
        try {
            request = (DealSearchRequest) Resources.getDefaultRequest(request);
            TenantContext context = tenantContextService.getCurrentContext();

            Sort.Direction direction = "DESC".equalsIgnoreCase(request.getOrderDir())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            String orderBy = (request.getOrderBy() != null && !request.getOrderBy().isBlank())
                    ? request.getOrderBy()
                    : "id";

            Pageable pageable = PageRequest.of(
                    Math.max(0, request.getPageNumber() - 1),
                    request.getPageSize(),
                    Sort.by(direction, orderBy)
            );

            Long targetOrgId = request.getOrganizationId();
            if (targetOrgId == null && context != null) {
                targetOrgId = context.getOrganizationId();
            }

            Page<Deal> page;
            if (targetOrgId != null) {
                page = dealRepository.findByOrganizationIdAndIsDeletedFalse(targetOrgId, pageable);
            } else {
                page = dealRepository.findByIsDeletedFalse(pageable);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.LIST_SUCCESS, LogConstants.DEAL);
            status.setDeals(page.getContent());
            status.setTotal(page.getTotalElements());
            return status;

        } catch (Exception e) {
            LOGGER.error("DealService >> listDeals exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus viewDeal(Long id) {
        LOGGER.info("DealService >> viewDeal called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Deal> dealOptional;
            if (context != null && context.getOrganizationId() != null) {
                dealOptional = dealRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                dealOptional = dealRepository.findByIdAndIsDeletedFalse(id);
            }

            if (dealOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DETAIL_FAILURE, LogConstants.DEAL);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.DETAIL_SUCCESS, LogConstants.DEAL);
            status.setDeal(dealOptional.get());
            return status;

        } catch (Exception e) {
            LOGGER.error("DealService >> viewDeal exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus createDeal(DealCreateRequest request) {
        LOGGER.info("DealService >> createDeal called for title: {}", request.getTitle());
        try {
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "title", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Long orgId = request.getOrganizationId();
            if (orgId == null && context != null) {
                orgId = context.getOrganizationId();
            }

            if (orgId == null) {
                return Resources.setStatus(Constants.FAILURE, "Organization ID is required.", null);
            }

            Optional<Organization> orgOptional = organizationRepository.findByIdAndIsDeletedFalse(orgId);
            if (orgOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, "Organization not found.", null);
            }

            Company company = null;
            if (request.getCompanyId() != null) {
                company = companyRepository.findByIdAndIsDeletedFalse(request.getCompanyId()).orElse(null);
            }

            Contact contact = null;
            if (request.getContactId() != null) {
                contact = contactRepository.findByIdAndIsDeletedFalse(request.getContactId()).orElse(null);
            }

            PipelineStage pipelineStage = null;
            if (request.getPipelineStageId() != null) {
                pipelineStage = pipelineStageRepository.findByIdAndIsDeletedFalse(request.getPipelineStageId()).orElse(null);
            }

            User assignedTo = null;
            if (request.getAssignedToId() != null) {
                assignedTo = userRepository.findByIdAndIsDeletedFalse(request.getAssignedToId()).orElse(null);
            }

            Deal deal = new Deal();
            deal.setOrganization(orgOptional.get());
            deal.setCompany(company);
            deal.setContact(contact);
            deal.setPipelineStage(pipelineStage);
            deal.setAssignedTo(assignedTo);
            deal.setTitle(request.getTitle().trim());
            deal.setAmount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO);
            deal.setCurrency(request.getCurrency() != null ? request.getCurrency().trim() : "USD");
            deal.setExpectedCloseDate(request.getExpectedCloseDate());
            deal.setStatus(request.getStatus() != null ? request.getStatus() : DealStatus.OPEN);
            deal.setIsActive(true);
            deal.setIsDeleted(false);

            Deal savedDeal = dealRepository.save(deal);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.DEAL, LogConstants.ADD, LocalDateTime.now(), null);
            auditLogService.logAction(savedDeal.getOrganization(), currentUser, LogConstants.DEAL, savedDeal.getId(), LogConstants.ADD, AuditSource.API, "Created deal: " + savedDeal.getTitle() + " (Amount: " + savedDeal.getAmount() + ")");

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.SAVE_SUCCESS, LogConstants.DEAL);
            status.setDeal(savedDeal);
            return status;

        } catch (Exception e) {
            LOGGER.error("DealService >> createDeal exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus editDeal(Long id, DealUpdateRequest request) {
        LOGGER.info("DealService >> editDeal called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Deal> dealOptional;
            if (context != null && context.getOrganizationId() != null) {
                dealOptional = dealRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                dealOptional = dealRepository.findByIdAndIsDeletedFalse(id);
            }

            if (dealOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.UPDATE_FAILURE, LogConstants.DEAL);
            }

            Deal deal = dealOptional.get();

            if (request.getCompanyId() != null) {
                Company company = companyRepository.findByIdAndIsDeletedFalse(request.getCompanyId()).orElse(null);
                deal.setCompany(company);
            }
            if (request.getContactId() != null) {
                Contact contact = contactRepository.findByIdAndIsDeletedFalse(request.getContactId()).orElse(null);
                deal.setContact(contact);
            }
            if (request.getPipelineStageId() != null) {
                PipelineStage stage = pipelineStageRepository.findByIdAndIsDeletedFalse(request.getPipelineStageId()).orElse(null);
                deal.setPipelineStage(stage);
            }
            if (request.getAssignedToId() != null) {
                User user = userRepository.findByIdAndIsDeletedFalse(request.getAssignedToId()).orElse(null);
                deal.setAssignedTo(user);
            }
            if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
                deal.setTitle(request.getTitle().trim());
            }
            if (request.getAmount() != null) {
                deal.setAmount(request.getAmount());
            }
            if (request.getCurrency() != null) {
                deal.setCurrency(request.getCurrency().trim());
            }
            if (request.getExpectedCloseDate() != null) {
                deal.setExpectedCloseDate(request.getExpectedCloseDate());
            }
            if (request.getStatus() != null) {
                deal.setStatus(request.getStatus());
            }
            if (request.getIsActive() != null) {
                deal.setIsActive(request.getIsActive());
            }

            deal.setUpdatedOn(LocalDateTime.now());
            Deal updatedDeal = dealRepository.save(deal);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.DEAL, LogConstants.EDIT, LocalDateTime.now(), null);
            auditLogService.logAction(updatedDeal.getOrganization(), currentUser, LogConstants.DEAL, updatedDeal.getId(), LogConstants.EDIT, AuditSource.API, "Updated deal: " + updatedDeal.getTitle());

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.UPDATE_SUCCESS, LogConstants.DEAL);
            status.setDeal(updatedDeal);
            return status;

        } catch (Exception e) {
            LOGGER.error("DealService >> editDeal exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus deleteDeal(Long id) {
        LOGGER.info("DealService >> deleteDeal called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Deal> dealOptional;
            if (context != null && context.getOrganizationId() != null) {
                dealOptional = dealRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                dealOptional = dealRepository.findByIdAndIsDeletedFalse(id);
            }

            if (dealOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DELETE_FAILURE, LogConstants.DEAL);
            }

            Deal deal = dealOptional.get();
            deal.setIsDeleted(true);
            deal.setUpdatedOn(LocalDateTime.now());
            dealRepository.save(deal);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.DEAL, LogConstants.DELETE, LocalDateTime.now(), null);
            auditLogService.logAction(deal.getOrganization(), currentUser, LogConstants.DEAL, deal.getId(), LogConstants.DELETE, AuditSource.API, "Soft-deleted deal: " + deal.getTitle());

            return Resources.setStatus(Constants.SUCCESS, Constants.DELETE_SUCCESS, LogConstants.DEAL);

        } catch (Exception e) {
            LOGGER.error("DealService >> deleteDeal exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus moveStage(Long id, DealMoveStageRequest request) {
        LOGGER.info("DealService >> moveStage called for deal id: {} to stage id: {}", id, request.getPipelineStageId());
        try {
            if (id == null || request.getPipelineStageId() == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id or pipelineStageId", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Deal> dealOptional;
            if (context != null && context.getOrganizationId() != null) {
                dealOptional = dealRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                dealOptional = dealRepository.findByIdAndIsDeletedFalse(id);
            }

            if (dealOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, "Deal not found.", null);
            }

            Optional<PipelineStage> stageOptional = pipelineStageRepository.findByIdAndIsDeletedFalse(request.getPipelineStageId());
            if (stageOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, "Pipeline stage not found.", null);
            }

            PipelineStage newStage = stageOptional.get();
            Deal deal = dealOptional.get();
            deal.setPipelineStage(newStage);

            if (request.getStatus() != null) {
                deal.setStatus(request.getStatus());
            } else if ("Closed Won".equalsIgnoreCase(newStage.getName())) {
                deal.setStatus(DealStatus.WON);
            } else if ("Closed Lost".equalsIgnoreCase(newStage.getName())) {
                deal.setStatus(DealStatus.LOST);
            }

            deal.setUpdatedOn(LocalDateTime.now());
            Deal updatedDeal = dealRepository.save(deal);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.DEAL, LogConstants.MOVE_STAGE, LocalDateTime.now(), null);
            auditLogService.logAction(updatedDeal.getOrganization(), currentUser, LogConstants.DEAL, updatedDeal.getId(), LogConstants.MOVE_STAGE, AuditSource.API, "Moved deal stage to: " + newStage.getName() + " (Status: " + updatedDeal.getStatus() + ")");

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, "Deal stage moved successfully.", LogConstants.DEAL);
            status.setDeal(updatedDeal);
            return status;

        } catch (Exception e) {
            LOGGER.error("DealService >> moveStage exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    private User resolveCurrentUser(TenantContext context) {
        if (context != null && context.getUserId() != null) {
            return userRepository.findById(context.getUserId()).orElse(null);
        }
        return null;
    }
}
