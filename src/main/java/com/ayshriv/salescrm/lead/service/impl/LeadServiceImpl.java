package com.ayshriv.salescrm.lead.service.impl;

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
import com.ayshriv.salescrm.deal.entity.Deal;
import com.ayshriv.salescrm.deal.entity.DealStatus;
import com.ayshriv.salescrm.deal.repository.DealRepository;
import com.ayshriv.salescrm.lead.dto.LeadConvertRequest;
import com.ayshriv.salescrm.lead.dto.LeadCreateRequest;
import com.ayshriv.salescrm.lead.dto.LeadSearchRequest;
import com.ayshriv.salescrm.lead.dto.LeadUpdateRequest;
import com.ayshriv.salescrm.lead.entity.Lead;
import com.ayshriv.salescrm.lead.entity.LeadStatus;
import com.ayshriv.salescrm.lead.repository.LeadRepository;
import com.ayshriv.salescrm.lead.service.LeadService;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.pipeline.entity.Pipeline;
import com.ayshriv.salescrm.pipeline.entity.PipelineStage;
import com.ayshriv.salescrm.pipeline.repository.PipelineRepository;
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
import java.util.List;
import java.util.Optional;

@Service
public class LeadServiceImpl implements LeadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeadServiceImpl.class);

    private final LeadRepository leadRepository;
    private final OrganizationRepository organizationRepository;
    private final CompanyRepository companyRepository;
    private final ContactRepository contactRepository;
    private final DealRepository dealRepository;
    private final PipelineRepository pipelineRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final UserRepository userRepository;
    private final TenantContextService tenantContextService;
    private final LogService logService;
    private final AuditLogService auditLogService;

    public LeadServiceImpl(
            LeadRepository leadRepository,
            OrganizationRepository organizationRepository,
            CompanyRepository companyRepository,
            ContactRepository contactRepository,
            DealRepository dealRepository,
            PipelineRepository pipelineRepository,
            PipelineStageRepository pipelineStageRepository,
            UserRepository userRepository,
            TenantContextService tenantContextService,
            LogService logService,
            AuditLogService auditLogService
    ) {
        this.leadRepository = leadRepository;
        this.organizationRepository = organizationRepository;
        this.companyRepository = companyRepository;
        this.contactRepository = contactRepository;
        this.dealRepository = dealRepository;
        this.pipelineRepository = pipelineRepository;
        this.pipelineStageRepository = pipelineStageRepository;
        this.userRepository = userRepository;
        this.tenantContextService = tenantContextService;
        this.logService = logService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus listLeads(LeadSearchRequest request) {
        LOGGER.info("LeadService >> listLeads called!");
        try {
            request = (LeadSearchRequest) Resources.getDefaultRequest(request);
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

            Page<Lead> page;
            if (targetOrgId != null) {
                String companyName = request.getCompanyName() != null && !request.getCompanyName().isBlank() ? request.getCompanyName().trim() : null;
                String email = request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail().trim() : null;
                String name = request.getName() != null && !request.getName().isBlank() ? request.getName().trim() : null;

                if (request.getStatus() != null || companyName != null || email != null || name != null) {
                    page = leadRepository.searchLeads(targetOrgId, request.getStatus(), companyName, email, name, pageable);
                } else {
                    page = leadRepository.findByOrganizationIdAndIsDeletedFalse(targetOrgId, pageable);
                }
            } else {
                page = leadRepository.findByIsDeletedFalse(pageable);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.LIST_SUCCESS, LogConstants.LEAD);
            status.setLeads(page.getContent());
            status.setTotal(page.getTotalElements());
            return status;

        } catch (Exception e) {
            LOGGER.error("LeadService >> listLeads exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus viewLead(Long id) {
        LOGGER.info("LeadService >> viewLead called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Lead> leadOptional;
            if (context != null && context.getOrganizationId() != null) {
                leadOptional = leadRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                leadOptional = leadRepository.findByIdAndIsDeletedFalse(id);
            }

            if (leadOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DETAIL_FAILURE, LogConstants.LEAD);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.DETAIL_SUCCESS, LogConstants.LEAD);
            status.setLead(leadOptional.get());
            return status;

        } catch (Exception e) {
            LOGGER.error("LeadService >> viewLead exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus createLead(LeadCreateRequest request) {
        LOGGER.info("LeadService >> createLead called for firstName: {}", request.getFirstName());
        try {
            if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "firstName", null);
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

            User assignedTo = null;
            if (request.getAssignedToId() != null) {
                assignedTo = userRepository.findByIdAndIsDeletedFalse(request.getAssignedToId()).orElse(null);
            }

            Lead lead = new Lead();
            lead.setOrganization(orgOptional.get());
            lead.setAssignedTo(assignedTo);
            lead.setFirstName(request.getFirstName().trim());
            lead.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
            lead.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
            lead.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
            lead.setCompanyName(request.getCompanyName() != null ? request.getCompanyName().trim() : null);
            lead.setJobTitle(request.getJobTitle() != null ? request.getJobTitle().trim() : null);
            lead.setStatus(request.getStatus() != null ? request.getStatus() : LeadStatus.NEW);
            lead.setSource(request.getSource() != null ? request.getSource().trim() : null);
            lead.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);
            lead.setIsActive(true);
            lead.setIsDeleted(false);

            Lead savedLead = leadRepository.save(lead);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.LEAD, LogConstants.ADD, LocalDateTime.now(), null);
            auditLogService.logAction(savedLead.getOrganization(), currentUser, LogConstants.LEAD, savedLead.getId(), LogConstants.ADD, AuditSource.API, "Created lead: " + savedLead.getFirstName() + " " + (savedLead.getLastName() != null ? savedLead.getLastName() : ""));

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.SAVE_SUCCESS, LogConstants.LEAD);
            status.setLead(savedLead);
            return status;

        } catch (Exception e) {
            LOGGER.error("LeadService >> createLead exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus editLead(Long id, LeadUpdateRequest request) {
        LOGGER.info("LeadService >> editLead called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Lead> leadOptional;
            if (context != null && context.getOrganizationId() != null) {
                leadOptional = leadRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                leadOptional = leadRepository.findByIdAndIsDeletedFalse(id);
            }

            if (leadOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.UPDATE_FAILURE, LogConstants.LEAD);
            }

            Lead lead = leadOptional.get();

            // Lifecycle transition validation
            if (request.getStatus() != null && request.getStatus() != lead.getStatus()) {
                if (!lead.getStatus().canTransitionTo(request.getStatus())) {
                    return Resources.setStatus(Constants.FAILURE, 
                            "Invalid status transition from " + lead.getStatus() + " to " + request.getStatus(), null);
                }
                lead.setStatus(request.getStatus());
            }

            if (request.getAssignedToId() != null) {
                User user = userRepository.findByIdAndIsDeletedFalse(request.getAssignedToId()).orElse(null);
                lead.setAssignedTo(user);
            }
            if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
                lead.setFirstName(request.getFirstName().trim());
            }
            if (request.getLastName() != null) {
                lead.setLastName(request.getLastName().trim());
            }
            if (request.getEmail() != null) {
                lead.setEmail(request.getEmail().trim());
            }
            if (request.getPhone() != null) {
                lead.setPhone(request.getPhone().trim());
            }
            if (request.getCompanyName() != null) {
                lead.setCompanyName(request.getCompanyName().trim());
            }
            if (request.getJobTitle() != null) {
                lead.setJobTitle(request.getJobTitle().trim());
            }
            if (request.getSource() != null) {
                lead.setSource(request.getSource().trim());
            }
            if (request.getNotes() != null) {
                lead.setNotes(request.getNotes().trim());
            }
            if (request.getIsActive() != null) {
                lead.setIsActive(request.getIsActive());
            }

            lead.setUpdatedOn(LocalDateTime.now());
            Lead updatedLead = leadRepository.save(lead);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.LEAD, LogConstants.EDIT, LocalDateTime.now(), null);
            auditLogService.logAction(updatedLead.getOrganization(), currentUser, LogConstants.LEAD, updatedLead.getId(), LogConstants.EDIT, AuditSource.API, "Updated lead: " + updatedLead.getFirstName() + " " + (updatedLead.getLastName() != null ? updatedLead.getLastName() : ""));

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.UPDATE_SUCCESS, LogConstants.LEAD);
            status.setLead(updatedLead);
            return status;

        } catch (Exception e) {
            LOGGER.error("LeadService >> editLead exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus deleteLead(Long id) {
        LOGGER.info("LeadService >> deleteLead called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Lead> leadOptional;
            if (context != null && context.getOrganizationId() != null) {
                leadOptional = leadRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                leadOptional = leadRepository.findByIdAndIsDeletedFalse(id);
            }

            if (leadOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DELETE_FAILURE, LogConstants.LEAD);
            }

            Lead lead = leadOptional.get();
            lead.setIsDeleted(true);
            lead.setUpdatedOn(LocalDateTime.now());
            leadRepository.save(lead);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.LEAD, LogConstants.DELETE, LocalDateTime.now(), null);
            auditLogService.logAction(lead.getOrganization(), currentUser, LogConstants.LEAD, lead.getId(), LogConstants.DELETE, AuditSource.API, "Soft-deleted lead: " + lead.getFirstName() + " " + (lead.getLastName() != null ? lead.getLastName() : ""));

            return Resources.setStatus(Constants.SUCCESS, Constants.DELETE_SUCCESS, LogConstants.LEAD);

        } catch (Exception e) {
            LOGGER.error("LeadService >> deleteLead exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus convertLead(Long id, LeadConvertRequest request) {
        LOGGER.info("LeadService >> convertLead called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Lead> leadOptional;
            if (context != null && context.getOrganizationId() != null) {
                leadOptional = leadRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                leadOptional = leadRepository.findByIdAndIsDeletedFalse(id);
            }

            if (leadOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, "Lead not found.", null);
            }

            Lead lead = leadOptional.get();
            if (lead.getStatus() == LeadStatus.CONVERTED) {
                return Resources.setStatus(Constants.FAILURE, "Lead is already converted.", null);
            }

            Organization org = lead.getOrganization();

            // 1. Resolve or Create Company
            Company company = null;
            if (request.getCompanyId() != null) {
                company = companyRepository.findByIdAndIsDeletedFalse(request.getCompanyId()).orElse(null);
            }

            String companyName = request.getCompanyName() != null && !request.getCompanyName().trim().isEmpty()
                    ? request.getCompanyName().trim()
                    : lead.getCompanyName();

            if (company == null && companyName != null && !companyName.trim().isEmpty()) {
                company = new Company();
                company.setOrganization(org);
                company.setName(companyName);
                company.setIsActive(true);
                company.setIsDeleted(false);
                company = companyRepository.save(company);
            }

            // 2. Create Contact
            Contact contact = new Contact();
            contact.setOrganization(org);
            contact.setCompany(company);
            contact.setFirstName(lead.getFirstName());
            contact.setLastName(lead.getLastName());
            contact.setEmail(lead.getEmail());
            contact.setPhone(lead.getPhone());
            contact.setJobTitle(request.getContactJobTitle() != null ? request.getContactJobTitle().trim() : lead.getJobTitle());
            contact.setDepartment(request.getContactDepartment() != null ? request.getContactDepartment().trim() : null);
            contact.setIsPrimary(true);
            contact.setIsActive(true);
            contact.setIsDeleted(false);
            Contact savedContact = contactRepository.save(contact);

            // 3. Optionally Create Deal
            Deal savedDeal = null;
            if (Boolean.TRUE.equals(request.getCreateDeal()) || request.getDealTitle() != null || request.getDealAmount() != null) {
                Deal deal = new Deal();
                deal.setOrganization(org);
                deal.setCompany(company);
                deal.setContact(savedContact);
                deal.setAssignedTo(lead.getAssignedTo());

                String defaultDealTitle = (lead.getFirstName() + " " + (lead.getLastName() != null ? lead.getLastName() : "") + " - Deal").trim();
                deal.setTitle(request.getDealTitle() != null && !request.getDealTitle().trim().isEmpty() ? request.getDealTitle().trim() : defaultDealTitle);
                deal.setAmount(request.getDealAmount() != null ? request.getDealAmount() : BigDecimal.ZERO);
                deal.setExpectedCloseDate(request.getExpectedCloseDate());
                deal.setStatus(DealStatus.OPEN);
                deal.setIsActive(true);
                deal.setIsDeleted(false);

                // Attach pipeline stage
                PipelineStage stage = null;
                if (request.getPipelineStageId() != null) {
                    stage = pipelineStageRepository.findByIdAndIsDeletedFalse(request.getPipelineStageId()).orElse(null);
                }
                if (stage == null) {
                    Optional<Pipeline> defaultPipeline = pipelineRepository.findByOrganizationIdAndIsDefaultTrueAndIsDeletedFalse(org.getId());
                    if (defaultPipeline.isPresent()) {
                        List<PipelineStage> stages = pipelineStageRepository.findByPipelineIdAndIsDeletedFalseOrderByOrderIndexAsc(defaultPipeline.get().getId());
                        if (!stages.isEmpty()) {
                            stage = stages.get(0);
                        }
                    }
                }
                deal.setPipelineStage(stage);
                savedDeal = dealRepository.save(deal);
            }

            // 4. Update Lead to CONVERTED
            lead.setStatus(LeadStatus.CONVERTED);
            lead.setConvertedContact(savedContact);
            lead.setConvertedCompany(company);
            lead.setConvertedDeal(savedDeal);
            lead.setConvertedAt(LocalDateTime.now());
            lead.setUpdatedOn(LocalDateTime.now());
            Lead updatedLead = leadRepository.save(lead);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.LEAD, LogConstants.CONVERT, LocalDateTime.now(), null);
            auditLogService.logAction(org, currentUser, LogConstants.LEAD, updatedLead.getId(), LogConstants.CONVERT, AuditSource.API, "Converted lead to contact: " + savedContact.getId() + ", company: " + (company != null ? company.getId() : "none") + ", deal: " + (savedDeal != null ? savedDeal.getId() : "none"));

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, "Lead converted successfully.", LogConstants.LEAD);
            status.setLead(updatedLead);
            status.setContact(savedContact);
            if (company != null) {
                status.setCompany(company);
            }
            if (savedDeal != null) {
                status.setDeal(savedDeal);
            }
            return status;

        } catch (Exception e) {
            LOGGER.error("LeadService >> convertLead exception: {}", e.getMessage(), e);
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
