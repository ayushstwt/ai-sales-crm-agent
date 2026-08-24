package com.ayshriv.salescrm.company.service.impl;

import com.ayshriv.salescrm.audit.entity.AuditSource;
import com.ayshriv.salescrm.audit.service.AuditLogService;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.resources.LogConstants;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.common.security.TenantContext;
import com.ayshriv.salescrm.common.security.TenantContextService;
import com.ayshriv.salescrm.company.dto.CompanyCreateRequest;
import com.ayshriv.salescrm.company.dto.CompanySearchRequest;
import com.ayshriv.salescrm.company.dto.CompanyUpdateRequest;
import com.ayshriv.salescrm.company.entity.Company;
import com.ayshriv.salescrm.company.repository.CompanyRepository;
import com.ayshriv.salescrm.company.service.CompanyService;
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
import java.util.Optional;

@Service
public class CompanyServiceImpl implements CompanyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyServiceImpl.class);

    private final CompanyRepository companyRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final TenantContextService tenantContextService;
    private final LogService logService;
    private final AuditLogService auditLogService;

    public CompanyServiceImpl(
            CompanyRepository companyRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            TenantContextService tenantContextService,
            LogService logService,
            AuditLogService auditLogService
    ) {
        this.companyRepository = companyRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.tenantContextService = tenantContextService;
        this.logService = logService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus listCompanies(CompanySearchRequest request) {
        LOGGER.info("CompanyService >> listCompanies called!");
        try {
            request = (CompanySearchRequest) Resources.getDefaultRequest(request);
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

            Page<Company> page;
            if (targetOrgId != null) {
                page = companyRepository.findByOrganizationIdAndIsDeletedFalse(targetOrgId, pageable);
            } else {
                page = companyRepository.findByIsDeletedFalse(pageable);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.LIST_SUCCESS, LogConstants.COMPANY);
            status.setCompanies(page.getContent());
            status.setTotal(page.getTotalElements());
            return status;

        } catch (Exception e) {
            LOGGER.error("CompanyService >> listCompanies exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus viewCompany(Long id) {
        LOGGER.info("CompanyService >> viewCompany called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Company> companyOptional;
            if (context != null && context.getOrganizationId() != null) {
                companyOptional = companyRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                companyOptional = companyRepository.findByIdAndIsDeletedFalse(id);
            }

            if (companyOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DETAIL_FAILURE, LogConstants.COMPANY);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.DETAIL_SUCCESS, LogConstants.COMPANY);
            status.setCompany(companyOptional.get());
            return status;

        } catch (Exception e) {
            LOGGER.error("CompanyService >> viewCompany exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus createCompany(CompanyCreateRequest request) {
        LOGGER.info("CompanyService >> createCompany called for name: {}", request.getName());
        try {
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "name", null);
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

            Company company = new Company();
            company.setOrganization(orgOptional.get());
            company.setName(request.getName().trim());
            company.setDomain(request.getDomain() != null ? request.getDomain().trim() : null);
            company.setWebsite(request.getWebsite() != null ? request.getWebsite().trim() : null);
            company.setIndustry(request.getIndustry() != null ? request.getIndustry().trim() : null);
            company.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
            company.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
            company.setCity(request.getCity() != null ? request.getCity().trim() : null);
            company.setState(request.getState() != null ? request.getState().trim() : null);
            company.setCountry(request.getCountry() != null ? request.getCountry().trim() : null);
            company.setPostalCode(request.getPostalCode() != null ? request.getPostalCode().trim() : null);
            company.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
            company.setIsActive(true);
            company.setIsDeleted(false);

            Company savedCompany = companyRepository.save(company);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.COMPANY, LogConstants.ADD, LocalDateTime.now(), null);
            auditLogService.logAction(savedCompany.getOrganization(), currentUser, LogConstants.COMPANY, savedCompany.getId(), LogConstants.ADD, AuditSource.API, "Created company: " + savedCompany.getName());

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.SAVE_SUCCESS, LogConstants.COMPANY);
            status.setCompany(savedCompany);
            return status;

        } catch (Exception e) {
            LOGGER.error("CompanyService >> createCompany exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus editCompany(Long id, CompanyUpdateRequest request) {
        LOGGER.info("CompanyService >> editCompany called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Company> companyOptional;
            if (context != null && context.getOrganizationId() != null) {
                companyOptional = companyRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                companyOptional = companyRepository.findByIdAndIsDeletedFalse(id);
            }

            if (companyOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.UPDATE_FAILURE, LogConstants.COMPANY);
            }

            Company company = companyOptional.get();

            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                company.setName(request.getName().trim());
            }
            if (request.getDomain() != null) {
                company.setDomain(request.getDomain().trim());
            }
            if (request.getWebsite() != null) {
                company.setWebsite(request.getWebsite().trim());
            }
            if (request.getIndustry() != null) {
                company.setIndustry(request.getIndustry().trim());
            }
            if (request.getPhone() != null) {
                company.setPhone(request.getPhone().trim());
            }
            if (request.getAddress() != null) {
                company.setAddress(request.getAddress().trim());
            }
            if (request.getCity() != null) {
                company.setCity(request.getCity().trim());
            }
            if (request.getState() != null) {
                company.setState(request.getState().trim());
            }
            if (request.getCountry() != null) {
                company.setCountry(request.getCountry().trim());
            }
            if (request.getPostalCode() != null) {
                company.setPostalCode(request.getPostalCode().trim());
            }
            if (request.getDescription() != null) {
                company.setDescription(request.getDescription().trim());
            }
            if (request.getIsActive() != null) {
                company.setIsActive(request.getIsActive());
            }

            company.setUpdatedOn(LocalDateTime.now());
            Company updatedCompany = companyRepository.save(company);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.COMPANY, LogConstants.EDIT, LocalDateTime.now(), null);
            auditLogService.logAction(updatedCompany.getOrganization(), currentUser, LogConstants.COMPANY, updatedCompany.getId(), LogConstants.EDIT, AuditSource.API, "Updated company: " + updatedCompany.getName());

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.UPDATE_SUCCESS, LogConstants.COMPANY);
            status.setCompany(updatedCompany);
            return status;

        } catch (Exception e) {
            LOGGER.error("CompanyService >> editCompany exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus deleteCompany(Long id) {
        LOGGER.info("CompanyService >> deleteCompany called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Company> companyOptional;
            if (context != null && context.getOrganizationId() != null) {
                companyOptional = companyRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                companyOptional = companyRepository.findByIdAndIsDeletedFalse(id);
            }

            if (companyOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DELETE_FAILURE, LogConstants.COMPANY);
            }

            Company company = companyOptional.get();
            company.setIsDeleted(true);
            company.setUpdatedOn(LocalDateTime.now());
            companyRepository.save(company);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.COMPANY, LogConstants.DELETE, LocalDateTime.now(), null);
            auditLogService.logAction(company.getOrganization(), currentUser, LogConstants.COMPANY, company.getId(), LogConstants.DELETE, AuditSource.API, "Soft-deleted company: " + company.getName());

            return Resources.setStatus(Constants.SUCCESS, Constants.DELETE_SUCCESS, LogConstants.COMPANY);

        } catch (Exception e) {
            LOGGER.error("CompanyService >> deleteCompany exception: {}", e.getMessage(), e);
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
