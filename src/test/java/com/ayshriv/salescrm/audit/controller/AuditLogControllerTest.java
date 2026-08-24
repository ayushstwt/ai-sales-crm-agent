package com.ayshriv.salescrm.audit.controller;

import com.ayshriv.salescrm.audit.entity.AuditLog;
import com.ayshriv.salescrm.audit.entity.AuditSource;
import com.ayshriv.salescrm.audit.repository.AuditLogRepository;
import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.security.UserPrincipal;
import com.ayshriv.salescrm.company.dto.CompanyCreateRequest;
import com.ayshriv.salescrm.deal.dto.DealCreateRequest;
import com.ayshriv.salescrm.deal.dto.DealMoveStageRequest;
import com.ayshriv.salescrm.lead.dto.LeadConvertRequest;
import com.ayshriv.salescrm.lead.dto.LeadCreateRequest;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.pipeline.dto.PipelineCreateRequest;
import com.ayshriv.salescrm.pipeline.entity.Pipeline;
import com.ayshriv.salescrm.pipeline.entity.PipelineStage;
import com.ayshriv.salescrm.pipeline.repository.PipelineRepository;
import com.ayshriv.salescrm.pipeline.repository.PipelineStageRepository;
import com.ayshriv.salescrm.user.entity.ERole;
import com.ayshriv.salescrm.user.entity.EUserType;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.entity.UserType;
import com.ayshriv.salescrm.user.repository.UserRepository;
import com.ayshriv.salescrm.user.repository.UserTypeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTypeRepository userTypeRepository;

    @Autowired
    private PipelineRepository pipelineRepository;

    @Autowired
    private PipelineStageRepository pipelineStageRepository;

    private Organization testOrg;
    private User testUser;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        pipelineStageRepository.deleteAll();
        pipelineRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        testOrg = organizationRepository.save(new Organization("Audit Corp", "audit-corp"));

        UserType adminType = userTypeRepository.findByName(EUserType.ORG_ADMIN)
                .orElseGet(() -> userTypeRepository.save(new UserType(EUserType.ORG_ADMIN, "Admin")));

        testUser = new User();
        testUser.setOrganization(testOrg);
        testUser.setUserType(adminType);
        testUser.setEmail("audit.tester@corp.com");
        testUser.setPassword("password");
        testUser.setFirstName("Audit");
        testUser.setLastName("Tester");
        testUser = userRepository.save(testUser);

        UserPrincipal principal = new UserPrincipal(
                testUser.getId(),
                testOrg.getId(),
                testUser.getEmail(),
                testUser.getPassword(),
                ERole.ROLE_ORG_ADMIN.name()
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(ERole.ROLE_ORG_ADMIN.name()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Creating a company writes an audit log and is listable via GET /audit-logs")
    void testCompanyCreationGeneratesAuditLog() throws Exception {
        CompanyCreateRequest companyRequest = new CompanyCreateRequest();
        companyRequest.setOrganizationId(testOrg.getId());
        companyRequest.setName("Omni Consumer Products");
        companyRequest.setIndustry("Robotics");

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS));

        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).hasSize(1);
        AuditLog log = logs.get(0);
        assertThat(log.getResourceType()).isEqualTo("COMPANY");
        assertThat(log.getAction()).isEqualTo("ADD");
        assertThat(log.getSource()).isEqualTo(AuditSource.API);
        assertThat(log.getOrganization().getId()).isEqualTo(testOrg.getId());

        // GET /audit-logs
        mockMvc.perform(get("/audit-logs")
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.auditLogs[0].resourceType").value("COMPANY"))
                .andExpect(jsonPath("$.auditLogs[0].action").value("ADD"));
    }

    @Test
    @DisplayName("Lead creation and conversion write distinct audit log entries")
    void testLeadAndConversionAuditLogs() throws Exception {
        // 1. Create Lead
        LeadCreateRequest leadReq = new LeadCreateRequest();
        leadReq.setOrganizationId(testOrg.getId());
        leadReq.setFirstName("John");
        leadReq.setLastName("Doe");
        leadReq.setEmail("john.doe@example.com");

        String createResp = mockMvc.perform(post("/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leadReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS))
                .andReturn().getResponse().getContentAsString();

        Long leadId = objectMapper.readTree(createResp).get("lead").get("id").asLong();

        // 2. Convert Lead
        LeadConvertRequest convertReq = new LeadConvertRequest();
        convertReq.setCompanyName("Doe Enterprises");
        convertReq.setCreateDeal(true);
        convertReq.setDealTitle("Doe Deal");
        convertReq.setDealAmount(new BigDecimal("25000.00"));

        mockMvc.perform(post("/leads/" + leadId + "/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(convertReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS));

        // Total audit logs: 1 (Lead Create) + 1 (Lead Convert)
        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).hasSizeGreaterThanOrEqualTo(2);

        AuditLog convertLog = logs.stream()
                .filter(l -> "CONVERT".equals(l.getAction()))
                .findFirst()
                .orElseThrow();
        assertThat(convertLog.getResourceType()).isEqualTo("LEAD");
        assertThat(convertLog.getResourceId()).isEqualTo(leadId);
    }
}
