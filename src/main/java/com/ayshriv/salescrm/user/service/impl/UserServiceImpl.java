package com.ayshriv.salescrm.user.service.impl;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.resources.LogConstants;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.common.security.TenantContext;
import com.ayshriv.salescrm.common.security.TenantContextService;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.user.dto.UserCreateRequest;
import com.ayshriv.salescrm.user.dto.UserSearchRequest;
import com.ayshriv.salescrm.user.dto.UserUpdateRequest;
import com.ayshriv.salescrm.user.entity.ERole;
import com.ayshriv.salescrm.user.entity.EUserType;
import com.ayshriv.salescrm.user.entity.Role;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.entity.UserType;
import com.ayshriv.salescrm.user.repository.RoleRepository;
import com.ayshriv.salescrm.user.repository.UserRepository;
import com.ayshriv.salescrm.user.repository.UserTypeRepository;
import com.ayshriv.salescrm.user.service.LogService;
import com.ayshriv.salescrm.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final UserTypeRepository userTypeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantContextService tenantContextService;
    private final LogService logService;

    public UserServiceImpl(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            UserTypeRepository userTypeRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            TenantContextService tenantContextService,
            LogService logService
    ) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.userTypeRepository = userTypeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tenantContextService = tenantContextService;
        this.logService = logService;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus listUsers(UserSearchRequest request) {
        LOGGER.info("UserService >> listUsers called!");
        try {
            request = (UserSearchRequest) Resources.getDefaultRequest(request);
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

            Page<User> page;
            if (targetOrgId != null) {
                page = userRepository.findByOrganizationIdAndIsDeletedFalse(targetOrgId, pageable);
            } else {
                page = userRepository.findByIsDeletedFalse(pageable);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.LIST_SUCCESS, LogConstants.USER);
            status.setUsers(page.getContent());
            status.setTotal(page.getTotalElements());
            return status;

        } catch (Exception e) {
            LOGGER.error("UserService >> listUsers exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus viewUser(Long id) {
        LOGGER.info("UserService >> viewUser called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            Optional<User> userOptional = userRepository.findByIdAndIsDeletedFalse(id);
            if (userOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DETAIL_FAILURE, LogConstants.USER);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.DETAIL_SUCCESS, LogConstants.USER);
            status.setUser(userOptional.get());
            return status;

        } catch (Exception e) {
            LOGGER.error("UserService >> viewUser exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus createUser(UserCreateRequest request) {
        LOGGER.info("UserService >> createUser called for email: {}", request.getEmail());
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "email", null);
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "password", null);
            }

            String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
            if (userRepository.existsByEmail(email)) {
                return Resources.setStatus(Constants.FAILURE, "User with email already exists.", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Long orgId = request.getOrganizationId();
            if (orgId == null && context != null) {
                orgId = context.getOrganizationId();
            }

            Organization organization = null;
            if (orgId != null) {
                organization = organizationRepository.findByIdAndIsDeletedFalse(orgId).orElse(null);
            }

            // Resolve UserType
            EUserType userTypeEnum = EUserType.SALES_REP;
            if (request.getUserType() != null) {
                try {
                    userTypeEnum = EUserType.valueOf(request.getUserType().toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ignored) {
                }
            }
            final EUserType finalType = userTypeEnum;
            UserType userType = userTypeRepository.findByName(finalType)
                    .orElseGet(() -> userTypeRepository.save(new UserType(finalType, finalType.name())));

            // Resolve Role
            ERole roleEnum = ERole.ROLE_SALES_REP;
            if (request.getRole() != null) {
                try {
                    roleEnum = ERole.valueOf(request.getRole().toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ignored) {
                }
            }
            final ERole finalRole = roleEnum;
            Role role = roleRepository.findByName(finalRole)
                    .orElseGet(() -> roleRepository.save(new Role(finalRole, finalRole.name())));

            User user = new User();
            user.setOrganization(organization);
            user.setUserType(userType);
            user.setRoles(new HashSet<>(Collections.singletonList(role)));
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : null);
            user.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
            user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
            user.setEmailVerified(true);
            user.setIsActive(true);
            user.setIsDeleted(false);

            User savedUser = userRepository.save(user);

            logService.createLog(savedUser, LogConstants.USER, LogConstants.ADD, LocalDateTime.now(), null);

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.SAVE_SUCCESS, LogConstants.USER);
            status.setUser(savedUser);
            return status;

        } catch (Exception e) {
            LOGGER.error("UserService >> createUser exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus editUser(Long id, UserUpdateRequest request) {
        LOGGER.info("UserService >> editUser called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            Optional<User> userOptional = userRepository.findByIdAndIsDeletedFalse(id);
            if (userOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.UPDATE_FAILURE, LogConstants.USER);
            }

            User user = userOptional.get();

            if (request.getFirstName() != null) {
                user.setFirstName(request.getFirstName().trim());
            }
            if (request.getLastName() != null) {
                user.setLastName(request.getLastName().trim());
            }
            if (request.getPhone() != null) {
                user.setPhone(request.getPhone().trim());
            }
            if (request.getIsActive() != null) {
                user.setIsActive(request.getIsActive());
            }

            if (request.getUserType() != null) {
                try {
                    EUserType ut = EUserType.valueOf(request.getUserType().toUpperCase(Locale.ROOT));
                    userTypeRepository.findByName(ut).ifPresent(user::setUserType);
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (request.getRole() != null) {
                try {
                    ERole r = ERole.valueOf(request.getRole().toUpperCase(Locale.ROOT));
                    roleRepository.findByName(r).ifPresent(role -> user.setRoles(new HashSet<>(Collections.singletonList(role))));
                } catch (IllegalArgumentException ignored) {
                }
            }

            user.setUpdatedOn(LocalDateTime.now());
            User updatedUser = userRepository.save(user);

            logService.createLog(updatedUser, LogConstants.USER, LogConstants.EDIT, LocalDateTime.now(), null);

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.UPDATE_SUCCESS, LogConstants.USER);
            status.setUser(updatedUser);
            return status;

        } catch (Exception e) {
            LOGGER.error("UserService >> editUser exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus deleteUser(Long id) {
        LOGGER.info("UserService >> deleteUser called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            Optional<User> userOptional = userRepository.findByIdAndIsDeletedFalse(id);
            if (userOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DELETE_FAILURE, LogConstants.USER);
            }

            User user = userOptional.get();
            user.setIsDeleted(true);
            user.setUpdatedOn(LocalDateTime.now());
            userRepository.save(user);

            logService.createLog(user, LogConstants.USER, LogConstants.DELETE, LocalDateTime.now(), null);

            return Resources.setStatus(Constants.SUCCESS, Constants.DELETE_SUCCESS, LogConstants.USER);

        } catch (Exception e) {
            LOGGER.error("UserService >> deleteUser exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }
}
