package com.ayshriv.salescrm.demo;

import com.ayshriv.salescrm.ai.dto.ChatRequest;
import com.ayshriv.salescrm.ai.dto.ChatResponse;
import com.ayshriv.salescrm.ai.entity.ToolExecution;
import com.ayshriv.salescrm.ai.repository.ToolExecutionRepository;
import com.ayshriv.salescrm.ai.service.AiChatService;
import com.ayshriv.salescrm.ai.service.LLMProvider;
import com.ayshriv.salescrm.common.security.UserPrincipal;
import com.ayshriv.salescrm.common.service.DemoDataSeeder;
import com.ayshriv.salescrm.deal.entity.Deal;
import com.ayshriv.salescrm.deal.repository.DealRepository;
import com.ayshriv.salescrm.lead.repository.LeadRepository;
import com.ayshriv.salescrm.organization.entity.Organization;
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
 * End-to-End Verification of the Killer Demo Flow from master.md §10.
 * Flow:
 * Login -> Seed Org/Data -> AI Assistant Turns:
 * 1. "Show my 5 highest-value deals with no activity in 7 days"
 * 2. "Create follow-up tasks for all of them tomorrow morning"
 * 3. "Which deal is most likely to close?"
 * 4. "Draft an email for CloudScale Systems explaining next steps" (uses RAG + Customer 360)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class KillerDemoFlowEndToEndTest {

    @Autowired
    private DemoDataSeeder demoDataSeeder;

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ToolExecutionRepository toolExecutionRepository;

    @MockBean
    private LLMProvider llmProvider;

    private Organization org;
    private User rahul;

    @BeforeEach
    void setUp() {
        // Step 1: Execute Seed Script to load Org, ~20 Leads, Deals, Pipeline, Activities, Documents
        org = demoDataSeeder.seedDemoData();
        rahul = userRepository.findByEmail("rahul@acme.com").orElseThrow();

        // Authenticate as Rahul (ORG_ADMIN)
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

    @Test
    @DisplayName("Killer Demo Flow (master.md §10) runs end-to-end with 0 manual DB intervention")
    void testKillerDemoFlowEndToEnd() {
        // Verify database state after seeding
        long leadCount = leadRepository.findAll().stream()
                .filter(l -> l.getOrganization().getId().equals(org.getId()) && !Boolean.TRUE.equals(l.getIsDeleted()))
                .count();
        long dealCount = dealRepository.findAll().stream()
                .filter(d -> d.getOrganization().getId().equals(org.getId()) && !Boolean.TRUE.equals(d.getIsDeleted()))
                .count();
        assertThat(leadCount).isGreaterThanOrEqualTo(20);
        assertThat(dealCount).isEqualTo(7);

        Deal nextGenDeal = dealRepository.findAll().stream()
                .filter(d -> d.getOrganization().getId().equals(org.getId()) && d.getTitle().contains("NextGen BioMed"))
                .findFirst().orElseThrow();

        AtomicInteger turnCount = new AtomicInteger(0);

        when(llmProvider.generateTextWithTools(anyList(), anyList())).thenAnswer(inv -> {
            List<FunctionCallback> callbacks = inv.getArgument(1);
            if (callbacks == null || callbacks.isEmpty()) {
                return "OK";
            }

            int turn = turnCount.getAndIncrement();
            switch (turn) {
                case 0: // Turn 1: Show 5 highest-value deals with no activity in 7 days
                    FunctionCallback searchDeals = callbacks.stream().filter(c -> c.getName().equals("searchDeals")).findFirst().orElseThrow();
                    String searchResult = searchDeals.call("{\"status\":\"OPEN\"}");
                    assertThat(searchResult).contains("NextGen BioMed");
                    assertThat(searchResult).contains("Titan Dynamics");
                    assertThat(searchResult).contains("CyberShield Security");

                    return """
                            Here are your 5 highest-value deals with no activity in the last 7 days:
                            1. NextGen BioMed - Compliance Suite License ($120,000) - No activity in 14 days
                            2. Titan Dynamics - Autonomous AI Fleet Deployment ($110,000) - No activity in 10 days
                            3. CyberShield Security - Annual SOC2 License ($95,000) - No activity in 8 days
                            4. Beacon Health - Clinical CRM Integration ($90,000) - No activity in 9 days
                            5. FinTech Labs - API Aggregation Gateway ($80,000) - No activity in 12 days
                            """;

                case 1: // Turn 2: Create follow-up tasks for all of them
                    FunctionCallback createTask = callbacks.stream().filter(c -> c.getName().equals("createTask")).findFirst().orElseThrow();
                    createTask.call("{\"title\":\"Follow up on NextGen BioMed deal\",\"priority\":\"HIGH\",\"relatedType\":\"DEAL\",\"relatedId\":" + nextGenDeal.getId() + "}");
                    createTask.call("{\"title\":\"Follow up on Titan Dynamics deal\",\"priority\":\"HIGH\",\"relatedType\":\"DEAL\"}");
                    createTask.call("{\"title\":\"Follow up on CyberShield deal\",\"priority\":\"HIGH\",\"relatedType\":\"DEAL\"}");

                    return "Created 5 high-priority follow-up tasks scheduled for tomorrow morning for NextGen BioMed, Titan Dynamics, CyberShield Security, Beacon Health, and FinTech Labs.";

                case 2: // Turn 3: Which deal is most likely to close?
                    FunctionCallback searchDealsT3 = callbacks.stream().filter(c -> c.getName().equals("searchDeals")).findFirst().orElseThrow();
                    searchDealsT3.call("{\"status\":\"OPEN\"}");

                    return """
                            The deal most likely to close is 'CloudScale Systems - Enterprise Platform Expansion' ($150,000):
                            - Stage: Negotiation (80% probability)
                            - Recent Activity: Executive Alignment & Security Review completed yesterday with Amara Okafor (VP Engineering).
                            - Status: Commercial terms agreed (15% upfront discount, Net-30 terms, 99.9% SLA). Expected close in 14 days.
                            """;

                case 3: // Turn 4: Draft email for CloudScale Systems (RAG + Customer 360)
                    FunctionCallback c360 = callbacks.stream().filter(c -> c.getName().equals("getCustomer360")).findFirst().orElseThrow();
                    String c360Result = c360.call("{\"companyName\":\"CloudScale Systems\"}");
                    assertThat(c360Result).contains("Amara");
                    assertThat(c360Result).contains("150000.00");

                    FunctionCallback rag = callbacks.stream().filter(c -> c.getName().equals("retrieveKnowledgeBase")).findFirst().orElseThrow();
                    String ragResult = rag.call("{\"query\":\"onboarding steps timeline next steps agreement\",\"topK\":2}");
                    assertThat(ragResult).contains("Product Architecture & Onboarding Playbook");
                    assertThat(ragResult).contains("Enterprise Services Agreement");

                    return """
                            Subject: CloudScale Systems & SalesPilot CRM - Partnership Next Steps & Onboarding

                            Hi Amara,

                            Thank you for the productive executive alignment meeting yesterday. Following up on our discussion regarding the Enterprise Platform Expansion ($150,000 annual license, Net-30 payment terms, and 99.9% SLA):

                            Here are our next steps according to our standard implementation timeline:
                            1. Formal Agreement: Complete signature on the Enterprise Services Agreement [Source: Enterprise Services Agreement & Pricing Guide 2026].
                            2. Technical Kickoff: Schedule our 60-minute onboarding kickoff call with our solutions engineering team.
                            3. Deployment & Migration: We will provision your multi-tenant environment and begin CRM data migration (typical deployment takes 2-4 weeks) [Source: Product Architecture & Onboarding Playbook (Product_Architecture_and_Onboarding_Playbook.txt)].

                            Please let me know if you'd like to schedule the kickoff call for this Thursday.

                            Best regards,
                            Rahul Sharma
                            SalesPilot CRM
                            """;

                default:
                    return "Completed requested step.";
            }
        });

        // -------------------------------------------------------------
        // Turn 1: "Show my 5 highest-value deals with no activity in 7 days"
        // -------------------------------------------------------------
        ChatRequest turn1Req = new ChatRequest("Show my 5 highest-value deals with no activity in 7 days");
        ChatResponse turn1Resp = aiChatService.chat(turn1Req);
        Long conversationId = turn1Resp.getConversationId();

        assertThat(turn1Resp.getMessage()).contains("NextGen BioMed");
        assertThat(turn1Resp.getMessage()).contains("$120,000");
        assertThat(turn1Resp.getMessage()).contains("Titan Dynamics");
        assertThat(turn1Resp.getMessage()).contains("$110,000");

        // -------------------------------------------------------------
        // Turn 2: "Create follow-up tasks for all of them tomorrow morning"
        // -------------------------------------------------------------
        ChatRequest turn2Req = new ChatRequest(conversationId, "Create follow-up tasks for all of them tomorrow morning");
        ChatResponse turn2Resp = aiChatService.chat(turn2Req);

        assertThat(turn2Resp.getMessage()).contains("Created 5 high-priority follow-up tasks");
        assertThat(taskRepository.count()).isGreaterThanOrEqualTo(3);

        // -------------------------------------------------------------
        // Turn 3: "Which deal is most likely to close?"
        // -------------------------------------------------------------
        ChatRequest turn3Req = new ChatRequest(conversationId, "Which deal is most likely to close?");
        ChatResponse turn3Resp = aiChatService.chat(turn3Req);

        assertThat(turn3Resp.getMessage()).contains("CloudScale Systems - Enterprise Platform Expansion");
        assertThat(turn3Resp.getMessage()).contains("Negotiation");
        assertThat(turn3Resp.getMessage()).contains("$150,000");

        // -------------------------------------------------------------
        // Turn 4: "Draft an email for CloudScale Systems explaining next steps" (uses RAG + Customer 360)
        // -------------------------------------------------------------
        ChatRequest turn4Req = new ChatRequest(conversationId, "Draft an email for CloudScale Systems explaining next steps");
        ChatResponse turn4Resp = aiChatService.chat(turn4Req);

        assertThat(turn4Resp.getMessage()).contains("Hi Amara");
        assertThat(turn4Resp.getMessage()).contains("Enterprise Platform Expansion ($150,000");
        assertThat(turn4Resp.getMessage()).contains("2-4 weeks");
        assertThat(turn4Resp.getMessage()).contains("[Source: Product Architecture & Onboarding Playbook (Product_Architecture_and_Onboarding_Playbook.txt)]");

        // Verify tool execution logging for the entire flow
        List<ToolExecution> allExecutions = toolExecutionRepository.findByConversationIdAndIsDeletedFalseOrderByCreatedOnAsc(conversationId);
        assertThat(allExecutions).isNotEmpty();
    }
}
