package com.ayshriv.salescrm.contact.service.impl;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.resources.LogConstants;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.common.security.TenantContext;
import com.ayshriv.salescrm.common.security.TenantContextService;
import com.ayshriv.salescrm.company.entity.Company;
import com.ayshriv.salescrm.company.repository.CompanyRepository;
import com.ayshriv.salescrm.contact.dto.ContactCreateRequest;
import com.ayshriv.salescrm.contact.dto.ContactSearchRequest;
import com.ayshriv.salescrm.contact.dto.ContactUpdateRequest;
import com.ayshriv.salescrm.contact.entity.Contact;
import com.ayshriv.salescrm.contact.repository.ContactRepository;
import com.ayshriv.salescrm.contact.service.ContactService;
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
public class ContactServiceImpl implements ContactService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactServiceImpl.class);

    private final ContactRepository contactRepository;
    private final OrganizationRepository organizationRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final TenantContextService tenantContextService;
    private final LogService logService;

    public ContactServiceImpl(
            ContactRepository contactRepository,
            OrganizationRepository organizationRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository,
            TenantContextService tenantContextService,
            LogService logService
    ) {
        this.contactRepository = contactRepository;
        this.organizationRepository = organizationRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.tenantContextService = tenantContextService;
        this.logService = logService;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus listContacts(ContactSearchRequest request) {
        LOGGER.info("ContactService >> listContacts called!");
        try {
            request = (ContactSearchRequest) Resources.getDefaultRequest(request);
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

            Page<Contact> page;
            if (targetOrgId != null) {
                page = contactRepository.findByOrganizationIdAndIsDeletedFalse(targetOrgId, pageable);
            } else {
                page = contactRepository.findByIsDeletedFalse(pageable);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.LIST_SUCCESS, LogConstants.CONTACT);
            status.setContacts(page.getContent());
            status.setTotal(page.getTotalElements());
            return status;

        } catch (Exception e) {
            LOGGER.error("ContactService >> listContacts exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus viewContact(Long id) {
        LOGGER.info("ContactService >> viewContact called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Contact> contactOptional;
            if (context != null && context.getOrganizationId() != null) {
                contactOptional = contactRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                contactOptional = contactRepository.findByIdAndIsDeletedFalse(id);
            }

            if (contactOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DETAIL_FAILURE, LogConstants.CONTACT);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.DETAIL_SUCCESS, LogConstants.CONTACT);
            status.setContact(contactOptional.get());
            return status;

        } catch (Exception e) {
            LOGGER.error("ContactService >> viewContact exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus createContact(ContactCreateRequest request) {
        LOGGER.info("ContactService >> createContact called for firstName: {}", request.getFirstName());
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

            Company company = null;
            if (request.getCompanyId() != null) {
                company = companyRepository.findByIdAndIsDeletedFalse(request.getCompanyId()).orElse(null);
            }

            Contact contact = new Contact();
            contact.setOrganization(orgOptional.get());
            contact.setCompany(company);
            contact.setFirstName(request.getFirstName().trim());
            contact.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
            contact.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
            contact.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
            contact.setJobTitle(request.getJobTitle() != null ? request.getJobTitle().trim() : null);
            contact.setDepartment(request.getDepartment() != null ? request.getDepartment().trim() : null);
            contact.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
            contact.setIsActive(true);
            contact.setIsDeleted(false);

            Contact savedContact = contactRepository.save(contact);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.CONTACT, LogConstants.ADD, LocalDateTime.now(), null);

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.SAVE_SUCCESS, LogConstants.CONTACT);
            status.setContact(savedContact);
            return status;

        } catch (Exception e) {
            LOGGER.error("ContactService >> createContact exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus editContact(Long id, ContactUpdateRequest request) {
        LOGGER.info("ContactService >> editContact called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Contact> contactOptional;
            if (context != null && context.getOrganizationId() != null) {
                contactOptional = contactRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                contactOptional = contactRepository.findByIdAndIsDeletedFalse(id);
            }

            if (contactOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.UPDATE_FAILURE, LogConstants.CONTACT);
            }

            Contact contact = contactOptional.get();

            if (request.getCompanyId() != null) {
                Company company = companyRepository.findByIdAndIsDeletedFalse(request.getCompanyId()).orElse(null);
                contact.setCompany(company);
            }
            if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
                contact.setFirstName(request.getFirstName().trim());
            }
            if (request.getLastName() != null) {
                contact.setLastName(request.getLastName().trim());
            }
            if (request.getEmail() != null) {
                contact.setEmail(request.getEmail().trim());
            }
            if (request.getPhone() != null) {
                contact.setPhone(request.getPhone().trim());
            }
            if (request.getJobTitle() != null) {
                contact.setJobTitle(request.getJobTitle().trim());
            }
            if (request.getDepartment() != null) {
                contact.setDepartment(request.getDepartment().trim());
            }
            if (request.getIsPrimary() != null) {
                contact.setIsPrimary(request.getIsPrimary());
            }
            if (request.getIsActive() != null) {
                contact.setIsActive(request.getIsActive());
            }

            contact.setUpdatedOn(LocalDateTime.now());
            Contact updatedContact = contactRepository.save(contact);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.CONTACT, LogConstants.EDIT, LocalDateTime.now(), null);

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.UPDATE_SUCCESS, LogConstants.CONTACT);
            status.setContact(updatedContact);
            return status;

        } catch (Exception e) {
            LOGGER.error("ContactService >> editContact exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus deleteContact(Long id) {
        LOGGER.info("ContactService >> deleteContact called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Contact> contactOptional;
            if (context != null && context.getOrganizationId() != null) {
                contactOptional = contactRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                contactOptional = contactRepository.findByIdAndIsDeletedFalse(id);
            }

            if (contactOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DELETE_FAILURE, LogConstants.CONTACT);
            }

            Contact contact = contactOptional.get();
            contact.setIsDeleted(true);
            contact.setUpdatedOn(LocalDateTime.now());
            contactRepository.save(contact);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.CONTACT, LogConstants.DELETE, LocalDateTime.now(), null);

            return Resources.setStatus(Constants.SUCCESS, Constants.DELETE_SUCCESS, LogConstants.CONTACT);

        } catch (Exception e) {
            LOGGER.error("ContactService >> deleteContact exception: {}", e.getMessage(), e);
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
