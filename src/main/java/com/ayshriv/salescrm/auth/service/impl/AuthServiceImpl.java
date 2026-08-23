package com.ayshriv.salescrm.auth.service.impl;

import com.ayshriv.salescrm.auth.dto.LoginRequest;
import com.ayshriv.salescrm.auth.dto.RegisterRequest;
import com.ayshriv.salescrm.auth.service.AuthService;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.common.security.JwtUtils;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.user.entity.ERole;
import com.ayshriv.salescrm.user.entity.EUserType;
import com.ayshriv.salescrm.user.entity.Role;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.entity.UserType;
import com.ayshriv.salescrm.user.repository.RoleRepository;
import com.ayshriv.salescrm.user.repository.UserRepository;
import com.ayshriv.salescrm.user.repository.UserTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final com.ayshriv.salescrm.user.service.LogService logService;

    public AuthServiceImpl(
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            UserTypeRepository userTypeRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            com.ayshriv.salescrm.user.service.LogService logService
    ) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.logService = logService;
    }

    @Override
    @Transactional
    public ApiStatus register(RegisterRequest request) {
        LOGGER.info("AuthService >> register called for email: {}", request.getEmail());
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "email", null);
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "password", null);
            }
            if (request.getOrganizationName() == null || request.getOrganizationName().trim().isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "organizationName", null);
            }

            String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
            if (userRepository.existsByEmail(email)) {
                return Resources.setStatus(Constants.FAILURE, "User with email already exists.", null);
            }

            // 1. Create Organization
            String slug = request.getOrganizationSlug();
            if (slug == null || slug.trim().isEmpty()) {
                slug = generateSlug(request.getOrganizationName());
            } else {
                slug = slug.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]", "-");
            }

            if (organizationRepository.existsBySlugAndIsDeletedFalse(slug)) {
                slug = slug + "-" + UUID.randomUUID().toString().substring(0, 6);
            }

            Organization organization = new Organization();
            organization.setName(request.getOrganizationName().trim());
            organization.setSlug(slug);
            organization.setIsActive(true);
            organization.setIsDeleted(false);
            organization = organizationRepository.save(organization);

            // 2. Lookup UserType (ORG_ADMIN)
            UserType orgAdminUserType = userTypeRepository.findByName(EUserType.ORG_ADMIN)
                    .orElseGet(() -> {
                        UserType ut = new UserType(EUserType.ORG_ADMIN, "Organization Administrator");
                        return userTypeRepository.save(ut);
                    });

            // 3. Lookup Role (ROLE_ORG_ADMIN)
            Role orgAdminRole = roleRepository.findByName(ERole.ROLE_ORG_ADMIN)
                    .orElseGet(() -> {
                        Role r = new Role(ERole.ROLE_ORG_ADMIN, "Organization Administrator Authority");
                        return roleRepository.save(r);
                    });

            // 4. Create User
            User user = new User();
            user.setOrganization(organization);
            user.setUserType(orgAdminUserType);
            user.setRoles(new HashSet<>(Collections.singletonList(orgAdminRole)));
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : null);
            user.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
            user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
            user.setIsActive(true);
            user.setIsDeleted(false);
            user = userRepository.save(user);

            // 5. Generate JWT Token
            String token = jwtUtils.generateToken(
                    user.getId(),
                    organization.getId(),
                    user.getEmail(),
                    orgAdminRole.getName().name()
            );

            // 6. Record user log
            logService.createLog(user, com.ayshriv.salescrm.common.resources.LogConstants.USER, com.ayshriv.salescrm.common.resources.LogConstants.SIGN_UP, java.time.LocalDateTime.now(), null);

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, "User and organization registered successfully.", null);
            status.setToken(token);
            status.setUser(user);
            status.setOrganization(organization);
            return status;

        } catch (Exception e) {
            LOGGER.error("AuthService >> register exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus login(LoginRequest request) {
        LOGGER.info("AuthService >> login called for email: {}", request.getEmail());
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "email", null);
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "password", null);
            }

            String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
            Optional<User> userOptional = userRepository.findByEmailAndIsDeletedFalse(email);
            if (userOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, "Invalid email or password.", null);
            }

            User user = userOptional.get();
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return Resources.setStatus(Constants.FAILURE, "Invalid email or password.", null);
            }

            if (Boolean.FALSE.equals(user.getIsActive())) {
                return Resources.setStatus(Constants.FAILURE, "User account is deactivated.", null);
            }

            String primaryRole = user.getRoles().stream()
                    .findFirst()
                    .map(r -> r.getName().name())
                    .orElse(ERole.ROLE_SALES_REP.name());

            String token = jwtUtils.generateToken(
                    user.getId(),
                    user.getOrganization().getId(),
                    user.getEmail(),
                    primaryRole
            );

            // Record user log
            logService.createLog(user, com.ayshriv.salescrm.common.resources.LogConstants.USER, com.ayshriv.salescrm.common.resources.LogConstants.SIGN_IN, java.time.LocalDateTime.now(), null);

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, "Login successful.", null);
            status.setToken(token);
            status.setUser(user);
            status.setOrganization(user.getOrganization());
            return status;

        } catch (Exception e) {
            LOGGER.error("AuthService >> login exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    private String generateSlug(String name) {
        String slug = name.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
        return slug.isEmpty() ? "org-" + UUID.randomUUID().toString().substring(0, 8) : slug;
    }
}
