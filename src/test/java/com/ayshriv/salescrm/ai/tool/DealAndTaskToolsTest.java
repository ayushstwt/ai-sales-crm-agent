package com.ayshriv.salescrm.ai.tool;

import com.ayshriv.salescrm.activity.dto.ActivityCreateRequest;
import com.ayshriv.salescrm.activity.entity.ActivityType;
import com.ayshriv.salescrm.activity.service.ActivityService;
import com.ayshriv.salescrm.ai.entity.ToolExecution;
import com.ayshriv.salescrm.ai.repository.ToolExecutionRepository;
import com.ayshriv.salescrm.ai.tool.dto.CreateTaskInput;
import com.ayshriv.salescrm.ai.tool.dto.CreateTaskOutput;
import com.ayshriv.salescrm.ai.tool.dto.CustomerTimelineInput;
import com.ayshriv.salescrm.ai.tool.dto.CustomerTimelineOutput;
import com.ayshriv.salescrm.ai.tool.dto.GetDealInput;
import com.ayshriv.salescrm.ai.tool.dto.GetDealOutput;
import com.ayshriv.salescrm.ai.tool.dto.SearchDealsInput;
import com.ayshriv.salescrm.ai.tool.dto.SearchDealsOutput;
import com.ayshriv.salescrm.ai.tool.dto.UpdateDealStageInput;
import com.ayshriv.salescrm.ai.tool.dto.UpdateDealStageOutput;
import com.ayshriv.salescrm.audit.entity.AuditLog;
import com.ayshriv.salescrm.audit.entity.AuditSource;
import com.ayshriv.salescrm.audit.repository.AuditLogRepository;
import com.ayshriv.salescrm.common.security.UserPrincipal;
import com.ayshriv.salescrm.company.dto.CompanyCreateRequest;
import com.ayshriv.salescrm.company.service.CompanyService;
import com.ayshriv.salescrm.deal.dto.DealCreateRequest;
import com.ayshriv.salescrm.deal.entity.Deal;
import com.ayshriv.salescrm.deal.entity.DealStatus;
import com.ayshriv.salescrm.deal.service.DealService;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.pipeline.dto.PipelineCreateRequest;
import com.ayshriv.salescrm.pipeline.entity.Pipeline;
import com.ayshriv.salescrm.pipeline.entity.PipelineStage;
import com.ayshriv.salescrm.pipeline.repository.PipelineRepository;
import com.ayshriv.salescrm.pipeline.repository.PipelineStageRepository;
import com.ayshriv.salescrm.task.repository.TaskRepository;
import com.ayshriv.salescrm.task.service.TaskService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DealAndTaskToolsTest {

    @Autowired
    private DealTools dealTools;

    @Autowired
    private TaskTools taskTools;

    @Autowired
    private ActivityTools activityTools;

    @SpyBean
    private DealService dealService;

    @SpyBean
    private TaskService taskService;

    @SpyBean
    private ActivityService activityService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private PipelineRepository pipelineRepository;

    @Autowired
    private PipelineStageRepository pipelineStageRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ToolExecutionRepository toolExecutionRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTypeRepository userTypeRepository;

    private Organization testOrg;
    private User testUser;
    private PipelineStage stageProspect;
    private PipelineStage stageClosedWon;
    private Long companyId;
    private Long dealId;

    @BeforeEach
    void setUp() {
        toolExecutionRepository.deleteAll();
        auditLogRepository.deleteAll();
        taskRepository.deleteAll();
        pipelineStageRepository.deleteAll();
        pipelineRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        testOrg = organizationRepository.save(new Organization("Tools Corp", "tools-corp"));

        UserType adminType = userTypeRepository.findByName(EUserType.ORG_ADMIN)
                .orElseGet(() -> userTypeRepository.save(new UserType(EUserType.ORG_ADMIN, "Admin")));

        testUser = new User();
        testUser.setOrganization(testOrg);
        testUser.setUserType(adminType);
        testUser.setEmail("tools.tester@corp.com");
        testUser.setPassword("password");
        testUser.setFirstName("Tools");
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

        // Seed Pipeline & Stages
        Pipeline pipeline = new Pipeline(testOrg, "Sales Pipeline");
        pipeline.setIsDefault(true);
        pipeline = pipelineRepository.save(pipeline);
        stageProspect = pipelineStageRepository.save(new PipelineStage(pipeline, "Prospecting", 0, 10.0));
        stageClosedWon = pipelineStageRepository.save(new PipelineStage(pipeline, "Closed Won", 1, 100.0));

        // Seed Company
        CompanyCreateRequest companyReq = new CompanyCreateRequest();
        companyReq.setOrganizationId(testOrg.getId());
        companyReq.setName("Acme Global");
        companyId = companyService.createCompany(companyReq).getCompany().getId();

        // Seed Deal
        DealCreateRequest dealReq = new DealCreateRequest();
        dealReq.setOrganizationId(testOrg.getId());
        dealReq.setTitle("Enterprise SaaS Package");
        dealReq.setAmount(new BigDecimal("50000.00"));
        dealReq.setCompanyId(companyId);
        dealReq.setPipelineStageId(stageProspect.getId());
        dealId = dealService.createDeal(dealReq).getDeal().getId();
    }

    @Test
    @DisplayName("Step 5.6: searchDeals and getDeal tools execute via DealService")
    void testDealReadOnlyTools() {
        // 1. searchDeals
        SearchDealsInput searchInput = new SearchDealsInput();
        searchInput.setTitle("Enterprise");
        SearchDealsOutput searchOutput = dealTools.searchDeals(searchInput);

        assertThat(searchOutput.getCount()).isEqualTo(1);
        assertThat(searchOutput.getDeals().get(0).getTitle()).isEqualTo("Enterprise SaaS Package");
        verify(dealService).listDeals(any());

        // 2. getDeal
        GetDealInput getInput = new GetDealInput(dealId);
        GetDealOutput getOutput = dealTools.getDeal(getInput);

        assertThat(getOutput.isFound()).isTrue();
        assertThat(getOutput.getDeal().getAmount()).isEqualByComparingTo("50000.00");
        verify(dealService).viewDeal(dealId);
    }

    @Test
    @DisplayName("Step 5.6: getCustomerTimeline tool returns activity timeline via ActivityService")
    void testCustomerTimelineTool() {
        // Seed Activity
        ActivityCreateRequest actReq = new ActivityCreateRequest();
        actReq.setOrganizationId(testOrg.getId());
        actReq.setCompanyId(companyId);
        actReq.setType(ActivityType.CALL);
        actReq.setTitle("Introductory Call");
        actReq.setDescription("Discussed requirements with customer.");
        activityService.createActivity(actReq);

        CustomerTimelineInput input = new CustomerTimelineInput();
        input.setCompanyId(companyId);

        CustomerTimelineOutput output = activityTools.getCustomerTimeline(input);

        assertThat(output.getCount()).isGreaterThanOrEqualTo(1);
        assertThat(output.getTimeline()).anyMatch(item -> item.getTitle().equals("Introductory Call"));
        verify(activityService).getCustomerTimeline(any(), any(), eq(companyId), any());
    }

    @Test
    @DisplayName("Step 5.7: createTask tool creates task and writes audit_logs with source: AI_AGENT")
    void testCreateTaskToolWritesAuditLogWithAiAgentSource() {
        CreateTaskInput input = new CreateTaskInput("Follow-up call with client", "Discuss contract terms", "HIGH");
        input.setRelatedType("COMPANY");
        input.setRelatedId(companyId);

        CreateTaskOutput output = taskTools.createTask(input);

        assertThat(output.isSuccess()).isTrue();
        assertThat(output.getTaskId()).isNotNull();
        assertThat(output.getPriority()).isEqualTo("HIGH");

        // Verify taskService called
        verify(taskService).createTask(any(), eq(AuditSource.AI_AGENT));

        // Verify tool execution logged
        List<ToolExecution> executions = toolExecutionRepository.findByToolNameAndIsDeletedFalseOrderByCreatedOnDesc("createTask");
        assertThat(executions).hasSize(1);
        assertThat(executions.get(0).getStatus()).isEqualTo("SUCCESS");

        // Verify audit_logs entry has source: AI_AGENT
        List<AuditLog> auditLogs = auditLogRepository.findByOrganizationIdAndResourceTypeAndIsDeletedFalse(
                testOrg.getId(), "TASK", org.springframework.data.domain.PageRequest.of(0, 10)
        ).getContent();

        assertThat(auditLogs).isNotEmpty();
        AuditLog taskAudit = auditLogs.stream()
                .filter(l -> l.getResourceId().equals(output.getTaskId()))
                .findFirst()
                .orElseThrow();
        assertThat(taskAudit.getSource()).isEqualTo(AuditSource.AI_AGENT);
    }

    @Test
    @DisplayName("Step 5.7: updateDealStage tool moves stage and writes audit_logs with source: AI_AGENT")
    void testUpdateDealStageToolWritesAuditLogWithAiAgentSource() {
        UpdateDealStageInput input = new UpdateDealStageInput(dealId, stageClosedWon.getId());

        UpdateDealStageOutput output = dealTools.updateDealStage(input);

        assertThat(output.isSuccess()).isTrue();
        assertThat(output.getStageName()).isEqualTo("Closed Won");
        assertThat(output.getStatus()).isEqualTo(DealStatus.WON.name());

        // Verify dealService called
        verify(dealService).moveStage(eq(dealId), any(), eq(AuditSource.AI_AGENT));

        // Verify audit_logs entry has source: AI_AGENT
        List<AuditLog> dealAudits = auditLogRepository.findByOrganizationIdAndResourceTypeAndIsDeletedFalse(
                testOrg.getId(), "DEAL", org.springframework.data.domain.PageRequest.of(0, 10)
        ).getContent();

        AuditLog moveAudit = dealAudits.stream()
                .filter(l -> "MOVE_STAGE".equals(l.getAction()) && l.getResourceId().equals(dealId))
                .findFirst()
                .orElseThrow();
        assertThat(moveAudit.getSource()).isEqualTo(AuditSource.AI_AGENT);
    }
}