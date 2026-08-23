package com.ayshriv.salescrm.auth.controller;

import com.ayshriv.salescrm.auth.dto.LoginRequest;
import com.ayshriv.salescrm.auth.dto.RegisterRequest;
import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.security.JwtUtils;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.user.entity.ERole;
import com.ayshriv.salescrm.user.entity.EUserType;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private com.ayshriv.salescrm.user.repository.UserLogRepository userLogRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        userLogRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setOrganizationName("Acme Corp");
        request.setEmail("admin@acme.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhone("+1234567890");

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("admin@acme.com"))
                .andExpect(jsonPath("$.organization.name").value("Acme Corp"))
                .andReturn();

        // Verify DB state
        User user = userRepository.findByEmail("admin@acme.com").orElseThrow();
        assertThat(user.getUserType().getName()).isEqualTo(EUserType.ORG_ADMIN);
        assertThat(user.getRoles()).anyMatch(r -> r.getName() == ERole.ROLE_ORG_ADMIN);
        assertThat(user.getOrganization().getName()).isEqualTo("Acme Corp");

        // Verify JWT claims
        String responseBody = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();
        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.getEmailFromToken(token)).isEqualTo("admin@acme.com");
        assertThat(jwtUtils.getUserIdFromToken(token)).isEqualTo(user.getId());
        assertThat(jwtUtils.getOrganizationIdFromToken(token)).isEqualTo(user.getOrganization().getId());
        assertThat(jwtUtils.getRoleFromToken(token)).isEqualTo(ERole.ROLE_ORG_ADMIN.name());

        // Verify UserLog created
        assertThat(userLogRepository.findByUserIdAndIsDeletedFalseOrderByCreatedOnDesc(user.getId()))
                .anyMatch(l -> "USER".equals(l.getAction()) && "SIGN_UP".equals(l.getSubAction()));
    }

    @Test
    void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest request1 = new RegisterRequest();
        request1.setOrganizationName("Acme Corp");
        request1.setEmail("admin@acme.com");
        request1.setPassword("password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS));

        RegisterRequest request2 = new RegisterRequest();
        request2.setOrganizationName("Beta LLC");
        request2.setEmail("admin@acme.com");
        request2.setPassword("password456");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.FAILURE))
                .andExpect(jsonPath("$.text").value("User with email already exists."));
    }

    @Test
    void testLoginSuccess() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setOrganizationName("Acme Corp");
        registerRequest.setEmail("admin@acme.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest("admin@acme.com", "password123");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("admin@acme.com"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();
        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.getRoleFromToken(token)).isEqualTo(ERole.ROLE_ORG_ADMIN.name());

        User user = userRepository.findByEmail("admin@acme.com").orElseThrow();
        assertThat(userLogRepository.findByUserIdAndIsDeletedFalseOrderByCreatedOnDesc(user.getId()))
                .anyMatch(l -> "USER".equals(l.getAction()) && "SIGN_IN".equals(l.getSubAction()));
    }

    @Test
    void testLoginInvalidPassword() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setOrganizationName("Acme Corp");
        registerRequest.setEmail("admin@acme.com");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest("admin@acme.com", "wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.FAILURE))
                .andExpect(jsonPath("$.text").value("Invalid email or password."));
    }

    @Test
    void testVerifyEmailAndResend() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setOrganizationName("Acme Corp");
        registerRequest.setEmail("verify@acme.com");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        User user = userRepository.findByEmail("verify@acme.com").orElseThrow();
        assertThat(user.getEmailVerified()).isFalse();
        String token = user.getEmailVerificationToken();
        assertThat(token).isNotNull();

        // Verify Email
        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new com.ayshriv.salescrm.auth.dto.VerifyEmailRequest(token))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS));

        User verifiedUser = userRepository.findByEmail("verify@acme.com").orElseThrow();
        assertThat(verifiedUser.getEmailVerified()).isTrue();
        assertThat(verifiedUser.getEmailVerificationToken()).isNull();

        // Resend on already verified returns failure
        mockMvc.perform(post("/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new com.ayshriv.salescrm.auth.dto.ResendVerificationRequest("verify@acme.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.FAILURE));
    }

    @Test
    void testForgotAndResetPassword() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setOrganizationName("Acme Corp");
        registerRequest.setEmail("reset@acme.com");
        registerRequest.setPassword("oldpassword123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Forgot password
        MvcResult forgotResult = mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new com.ayshriv.salescrm.auth.dto.ForgotPasswordRequest("reset@acme.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS))
                .andReturn();

        User user = userRepository.findByEmail("reset@acme.com").orElseThrow();
        String resetToken = user.getPasswordResetToken();
        assertThat(resetToken).isNotNull();

        // Reset password
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new com.ayshriv.salescrm.auth.dto.ResetPasswordRequest(resetToken, "newpassword123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS));

        // Login with new password
        LoginRequest loginRequest = new LoginRequest("reset@acme.com", "newpassword123");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS));
    }
}
