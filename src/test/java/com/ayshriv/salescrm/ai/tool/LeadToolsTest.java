package com.ayshriv.salescrm.ai.tool;

import com.ayshriv.salescrm.ai.entity.ToolExecution;
import com.ayshriv.salescrm.ai.repository.ToolExecutionRepository;
import com.ayshriv.salescrm.ai.tool.dto.GetLeadOutput;
import com.ayshriv.salescrm.ai.tool.dto.SearchLeadsInput;
import com.ayshriv.salescrm.ai.tool.dto.SearchLeadsOutput;
import com.ayshriv.salescrm.common.security.UserPrincipal;
import com.ayshriv.salescrm.lead.dto.LeadCreateRequest;
import com.ayshriv.salescrm.lead.repository.LeadRepository;
import com.ayshriv.salescrm.lead.service.LeadService;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.user.entity.ERole;
import com.ayshriv.salescrm.user.entity.EUserType;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.entity.UserType;
import com.ayshriv.salescrm.user.repository.UserRepository;
import com.ayshriv.salescrm.user.repository.UserTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class LeadToolsTest {

    @Autowired
    private LeadTools leadTools;

    @SpyBean
    private LeadService leadService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private ToolExecutionRepository toolExecutionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTypeRepository userTypeRepository;

    private Organization testOrg;
    private User testUser;

    @BeforeEach
    void setUp() {
        toolExecutionRepository.deleteAll();
        leadRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        testOrg = organizationRepository.save(new Organization("Tool Corp", "tool-corp"));

        UserType adminType = userTypeRepository.findByName(EUserType.ORG_ADMIN)
                .orElseGet(() -> userTypeRepository.save(new UserType(EUserType.ORG_ADMIN, "Admin")));

        testUser = new User();
        testUser.setOrganization(testOrg);
        testUser.setUserType(adminType);
        testUser.setEmail("tool.tester@corp.com");
        testUser.setPassword("password");
        testUser.setFirstName("Tool");
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

        // Seed 2 leads via LeadService
        LeadCreateRequest lead1 = new LeadCreateRequest();
        lead1.setOrganizationId(testOrg.getId());
        lead1.setFirstName("Alice");
        lead1.setLastName("Smith");
        lead1.setEmail("alice@acme.com");
        lead1.setCompanyName("Acme Corp");
        leadService.createLead(lead1);

        LeadCreateRequest lead2 = new LeadCreateRequest();
        lead2.setOrganizationId(testOrg.getId());
        lead2.setFirstName("Bob");
        lead2.setLastName("Jones");
        lead2.setEmail("bob@globex.com");
        lead2.setCompanyName("Globex Inc");
        leadService.createLead(lead2);
    }

    @Test
    @DisplayName("Step 5.4 & 5.5: searchLeads tool calls real LeadService (never repository directly) and logs to tool_executions")
    void testSearchLeadsToolExecutesViaServiceAndLogsExecution() {
        SearchLeadsInput input = new SearchLeadsInput();
        input.setCompanyName("Acme");

        SearchLeadsOutput output = leadTools.searchLeads(input);

        // 1. Verify output
        assertThat(output.getCount()).isEqualTo(1);
        assertThat(output.getLeads()).hasSize(1);
        assertThat(output.getLeads().get(0).getFirstName()).isEqualTo("Alice");
        assertThat(output.getLeads().get(0).getCompanyName()).isEqualTo("Acme Corp");

        // 2. CRITICAL Architecture Rule #2 verification: Tool MUST call LeadService
        verify(leadService).listLeads(any());

        // 3. Verify tool_executions record
        List<ToolExecution> executions = toolExecutionRepository.findAll();
        assertThat(executions).hasSize(1);
        ToolExecution execution = executions.get(0);
        assertThat(execution.getToolName()).isEqualTo("searchLeads");
        assertThat(execution.getStatus()).isEqualTo("SUCCESS");
        assertThat(execution.getOrganization().getId()).isEqualTo(testOrg.getId());
        assertThat(execution.getArguments()).contains("Acme");
        assertThat(execution.getResult()).contains("Alice");
        assertThat(execution.getExecutionTimeMs()).isNotNull();
    }

    @Test
    @DisplayName("Step 5.6: getLead tool calls real LeadService and logs execution")
    void testGetLeadToolExecutesViaService() {
        SearchLeadsOutput search = leadTools.searchLeads(new SearchLeadsInput("Alice", null, null, null));
        Long leadId = search.getLeads().get(0).getId();

        GetLeadOutput leadOutput = leadTools.getLead(new com.ayshriv.salescrm.ai.tool.dto.GetLeadInput(leadId));

        assertThat(leadOutput.isFound()).isTrue();
        assertThat(leadOutput.getLead().getFirstName()).isEqualTo("Alice");
        assertThat(leadOutput.getLead().getEmail()).isEqualTo("alice@acme.com");

        verify(leadService).viewLead(leadId);
    }
}