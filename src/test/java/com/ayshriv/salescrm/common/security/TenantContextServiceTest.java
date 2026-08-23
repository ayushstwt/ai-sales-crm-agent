package com.ayshriv.salescrm.common.security;

import com.ayshriv.salescrm.common.security.impl.TenantContextServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextServiceTest {

    private TenantContextService tenantContextService;

    @BeforeEach
    void setUp() {
        tenantContextService = new TenantContextServiceImpl();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentContext_whenUserPrincipalAuthenticated_returnsTenantContext() {
        UserPrincipal principal = new UserPrincipal(101L, 501L, "rahul@crm.com", "pass", "ROLE_ORG_ADMIN");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        TenantContext context = tenantContextService.getCurrentContext();

        assertNotNull(context);
        assertEquals(101L, context.getUserId());
        assertEquals(501L, context.getOrganizationId());
        assertEquals("ROLE_ORG_ADMIN", context.getRole());
    }

    @Test
    void getCurrentContext_whenNotAuthenticated_returnsNull() {
        TenantContext context = tenantContextService.getCurrentContext();
        assertNull(context);
    }
}
