package com.ayshriv.salescrm.common.security.impl;

import com.ayshriv.salescrm.common.security.TenantContext;
import com.ayshriv.salescrm.common.security.TenantContextService;
import com.ayshriv.salescrm.common.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TenantContextServiceImpl implements TenantContextService {

    @Override
    public TenantContext getCurrentContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return new TenantContext(
                    userPrincipal.getUserId(),
                    userPrincipal.getOrganizationId(),
                    userPrincipal.getRole()
            );
        }

        if (principal instanceof TenantContext tenantContext) {
            return tenantContext;
        }

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        return new TenantContext(null, null, role);
    }
}
