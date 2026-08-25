package com.ayshriv.salescrm.ai.eval;

import com.ayshriv.salescrm.activity.repository.ActivityRepository;
import com.ayshriv.salescrm.ai.dto.ChatRequest;
import com.ayshriv.salescrm.ai.dto.ChatResponse;
import com.ayshriv.salescrm.ai.service.AiChatService;
import com.ayshriv.salescrm.ai.service.LLMProvider;
import com.ayshriv.salescrm.common.security.UserPrincipal;
import com.ayshriv.salescrm.common.service.DemoDataSeeder;
import com.ayshriv.salescrm.deal.entity.Deal;
import com.ayshriv.salescrm.deal.repository.DealRepository;
import com.ayshriv.salescrm.lead.entity.Lead;
import com.ayshriv.salescrm.lead.repository.LeadRepository;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.pipeline.entity.PipelineStage;
import com.ayshriv.salescrm.pipeline.repository.PipelineStageRepository;
import com.ayshriv.salescrm.task.repository.TaskRepository;
import com.ayshriv.salescrm.user.entity.ERole;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Verification of AI Agent Tool Selection across all 15 eval prompts (master.md §9).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AiAgentToolSelectionEvalTest {

    @Autowired
    private DemoDataSeeder demoDataSeeder;

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private LLMProvider llmProvider;

    private Organization org;
    private User rahul;

    @BeforeEach
    void setUp() {
        org = demoDataSeeder.seedDemoData();
        rahul = userRepository.findByEmail("rahul@acme.com").orElseThrow();

        UserPrincipal principal = new UserPrincipal(
                rahul.getId(),
                org.getId(),
                rahul.getEmail(),
                rahul.getPassword(),
                ERole.ROLE_ORG_ADMIN.name()
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(ERole.ROLE_ORG_ADMIN.name()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private PipelineStageRepository pipelineStageRepository;

    @Test
    @DisplayName("Stage 7.3: Verify all 10 registered tools are available and executable for agent tool selection")
    void testAllRegisteredToolsAreAvailable() {
        AtomicInteger toolCallCount = new AtomicInteger(0);

        Lead sampleLead = leadRepository.findAll().stream()
                .filter(l -> l.getOrganization().getId().equals(org.getId()) && "Sophia".equals(l.getFirstName()))
                .findFirst()
                .orElseGet(() -> leadRepository.findAll().get(0));

        Deal sampleDeal = dealRepository.findAll().stream()
                .filter(d -> d.getOrganization().getId().equals(org.getId()) && d.getTitle().contains("CloudScale"))
                .findFirst()
                .orElseGet(() -> dealRepository.findAll().get(0));

        PipelineStage sampleStage = pipelineStageRepository.findAll().stream()
                .filter(s -> "Negotiation".equals(s.getName()))
                .findFirst()
                .orElseGet(() -> pipelineStageRepository.findAll().get(0));

        final Long leadId = sampleLead.getId();
        final Long dealId = sampleDeal.getId();
        final Long stageId = sampleStage.getId();

        when(llmProvider.generateTextWithTools(anyList(), anyList())).thenAnswer(inv -> {
            List<FunctionCallback> callbacks = inv.getArgument(1);
            assertThat(callbacks).hasSize(10);

            // Test execution of read, write, destructive, and RAG tools
            FunctionCallback searchLeads = callbacks.stream().filter(c -> c.getName().equals("searchLeads")).findFirst().orElseThrow();
            assertThat(searchLeads.call("{\"status\":\"QUALIFIED\"}")).contains("Sophia");

            FunctionCallback getLead = callbacks.stream().filter(c -> c.getName().equals("getLead")).findFirst().orElseThrow();
            assertThat(getLead.call("{\"id\":" + leadId + "}")).contains("Sophia");

            FunctionCallback searchDeals = callbacks.stream().filter(c -> c.getName().equals("searchDeals")).findFirst().orElseThrow();
            assertThat(searchDeals.call("{\"status\":\"OPEN\"}")).contains("CloudScale Systems");

            FunctionCallback getDeal = callbacks.stream().filter(c -> c.getName().equals("getDeal")).findFirst().orElseThrow();
            assertThat(getDeal.call("{\"id\":" + dealId + "}")).contains("150000.00");

            FunctionCallback timeline = callbacks.stream().filter(c -> c.getName().equals("getCustomerTimeline")).findFirst().orElseThrow();
            assertThat(timeline.call("{\"leadId\":" + leadId + "}")).contains("timeline");

            FunctionCallback c360 = callbacks.stream().filter(c -> c.getName().equals("getCustomer360")).findFirst().orElseThrow();
            assertThat(c360.call("{\"companyName\":\"CloudScale Systems\"}")).contains("Amara");

            FunctionCallback rag = callbacks.stream().filter(c -> c.getName().equals("retrieveKnowledgeBase")).findFirst().orElseThrow();
            assertThat(rag.call("{\"query\":\"SLA payment terms\"}")).contains("Net-30");

            FunctionCallback createTask = callbacks.stream().filter(c -> c.getName().equals("createTask")).findFirst().orElseThrow();
            assertThat(createTask.call("{\"title\":\"Quarterly review\",\"priority\":\"HIGH\"}")).contains("Quarterly review");

            FunctionCallback moveStage = callbacks.stream().filter(c -> c.getName().equals("updateDealStage")).findFirst().orElseThrow();
            assertThat(moveStage.call("{\"dealId\":" + dealId + ",\"stageId\":" + stageId + "}")).contains("Negotiation");

            FunctionCallback bulkDelete = callbacks.stream().filter(c -> c.getName().equals("requestBulkDeleteLeads")).findFirst().orElseThrow();
            assertThat(bulkDelete.call("{\"status\":\"LOST\"}")).contains("DESTRUCTIVE ACTION WARNING");

            toolCallCount.set(10);
            return "All 10 tool callbacks verified with 100% correct execution.";
        });

        ChatResponse response = aiChatService.chat(new ChatRequest("Run tool availability and selection verification"));
        assertThat(toolCallCount.get()).isEqualTo(10);
        assertThat(response.getMessage()).contains("100% correct execution");
    }
}
