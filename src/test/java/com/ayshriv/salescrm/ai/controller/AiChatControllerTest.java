package com.ayshriv.salescrm.ai.controller;

import com.ayshriv.salescrm.ai.dto.ChatRequest;
import com.ayshriv.salescrm.ai.entity.Conversation;
import com.ayshriv.salescrm.ai.entity.ConversationMessage;
import com.ayshriv.salescrm.ai.entity.MessageRole;
import com.ayshriv.salescrm.ai.entity.ToolExecution;
import com.ayshriv.salescrm.ai.repository.ConversationMessageRepository;
import com.ayshriv.salescrm.ai.repository.ConversationRepository;
import com.ayshriv.salescrm.ai.repository.ToolExecutionRepository;
import com.ayshriv.salescrm.ai.service.LLMProvider;
import com.ayshriv.salescrm.ai.service.impl.AzureOpenAIProvider;
import com.ayshriv.salescrm.common.security.UserPrincipal;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
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
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AiChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationMessageRepository conversationMessageRepository;

    @Autowired
    private ToolExecutionRepository toolExecutionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTypeRepository userTypeRepository;

    @MockBean
    private LLMProvider llmProvider;

    @Autowired
    private AzureOpenAIProvider azureOpenAIProvider;

    private Organization testOrg;
    private User testUser;

    @BeforeEach
    void setUp() {
        toolExecutionRepository.deleteAll();
        conversationMessageRepository.deleteAll();
        conversationRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        testOrg = organizationRepository.save(new Organization("AI Corp", "ai-corp"));

        UserType adminType = userTypeRepository.findByName(EUserType.ORG_ADMIN)
                .orElseGet(() -> userTypeRepository.save(new UserType(EUserType.ORG_ADMIN, "Admin")));

        testUser = new User();
        testUser.setOrganization(testOrg);
        testUser.setUserType(adminType);
        testUser.setEmail("ai.tester@corp.com");
        testUser.setPassword("password");
        testUser.setFirstName("AI");
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
    @DisplayName("Step 5.1: Raw completion throwaway endpoint works with LLMProvider")
    void testRawCompletionEndpoint() throws Exception {
        when(llmProvider.generateText("Hello")).thenReturn("Hello from LLM!");

        mockMvc.perform(get("/ai/completion").param("prompt", "Hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prompt").value("Hello"))
                .andExpect(jsonPath("$.completion").value("Hello from LLM!"));

        mockMvc.perform(post("/ai/completion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("prompt", "Hello"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prompt").value("Hello"))
                .andExpect(jsonPath("$.completion").value("Hello from LLM!"));
    }

    @Test
    @DisplayName("Step 5.1: Azure OpenAI provider throws UnsupportedOperationException as a stub")
    void testAzureOpenAIProviderIsStub() {
        assertThatThrownBy(() -> azureOpenAIProvider.generateText("test"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("stub per master.md decision #1");
    }

    @Test
    @DisplayName("Step 5.2: POST /ai/chat creates conversation, persists user & assistant messages, returns ChatResponse DTO")
    void testChatRoundTripCreatesConversationAndPersistsMessages() throws Exception {
        when(llmProvider.generateTextWithTools(anyList(), anyList())).thenReturn("I am your AI sales assistant.");

        ChatRequest request = new ChatRequest("What deals need follow-up today?");

        String responseJson = mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").isNotEmpty())
                .andExpect(jsonPath("$.message").value("I am your AI sales assistant."))
                .andExpect(jsonPath("$.role").value("ASSISTANT"))
                .andExpect(jsonPath("$.statusType").doesNotExist()) // Verify NOT ApiStatus wrapper
                .andReturn().getResponse().getContentAsString();

        Long conversationId = objectMapper.readTree(responseJson).get("conversationId").asLong();

        // Verify conversation is persisted in DB
        Conversation savedConv = conversationRepository.findById(conversationId).orElseThrow();
        assertThat(savedConv.getOrganization().getId()).isEqualTo(testOrg.getId());
        assertThat(savedConv.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(savedConv.getTitle()).isEqualTo("What deals need follow-up today?");

        // Verify both user and assistant messages are in DB
        List<ConversationMessage> messages = conversationMessageRepository
                .findByConversationIdAndOrganizationIdAndIsDeletedFalseOrderByCreatedOnAsc(conversationId, testOrg.getId());

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getRole()).isEqualTo(MessageRole.USER);
        assertThat(messages.get(0).getContent()).isEqualTo("What deals need follow-up today?");
        assertThat(messages.get(1).getRole()).isEqualTo(MessageRole.ASSISTANT);
        assertThat(messages.get(1).getContent()).isEqualTo("I am your AI sales assistant.");
    }

    @Test
    @DisplayName("Step 5.2: Multi-turn chat appends to existing conversation and passes conversation history to LLM")
    void testMultiTurnChatConversation() throws Exception {
        when(llmProvider.generateTextWithTools(anyList(), anyList()))
                .thenReturn("First answer.")
                .thenReturn("Second answer with context.");

        // Turn 1
        ChatRequest req1 = new ChatRequest("Turn 1 question");
        String resp1 = mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long conversationId = objectMapper.readTree(resp1).get("conversationId").asLong();

        // Turn 2 with existing conversationId
        ChatRequest req2 = new ChatRequest(conversationId, "Turn 2 question");
        mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(conversationId))
                .andExpect(jsonPath("$.message").value("Second answer with context."));

        // Check messages count is 4 (USER, ASSISTANT, USER, ASSISTANT)
        List<ConversationMessage> messages = conversationMessageRepository
                .findByConversationIdAndOrganizationIdAndIsDeletedFalseOrderByCreatedOnAsc(conversationId, testOrg.getId());

        assertThat(messages).hasSize(4);
        assertThat(messages.get(0).getContent()).isEqualTo("Turn 1 question");
        assertThat(messages.get(1).getContent()).isEqualTo("First answer.");
        assertThat(messages.get(2).getContent()).isEqualTo("Turn 2 question");
        assertThat(messages.get(3).getContent()).isEqualTo("Second answer with context.");

        // Check history retrieval endpoint
        mockMvc.perform(get("/ai/conversations/" + conversationId + "/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[1].role").value("ASSISTANT"));
    }

    @Test
    @DisplayName("Step 5.4 & 5.6 & 5.7: Chat with tool invocation triggers tools and logs execution")
    void testChatTriggersToolExecution() throws Exception {
        when(llmProvider.generateTextWithTools(anyList(), anyList())).thenAnswer(invocation -> {
            List<FunctionCallback> callbacks = invocation.getArgument(1);
            assertThat(callbacks).hasSize(7);
            
            FunctionCallback searchLeads = callbacks.stream().filter(c -> c.getName().equals("searchLeads")).findFirst().orElseThrow();
            String toolResult = searchLeads.call("{\"companyName\":\"Acme\"}");
            assertThat(toolResult).contains("leads");

            return "I found 0 leads for Acme.";
        });

        ChatRequest request = new ChatRequest("Find leads for Acme");
        mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("I found 0 leads for Acme."));

        List<ToolExecution> executions = toolExecutionRepository.findAll();
        assertThat(executions).hasSize(1);
        assertThat(executions.get(0).getToolName()).isEqualTo("searchLeads");
        assertThat(executions.get(0).getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("Step 5.7: Chat invoking write tool creates task and logs audit")
    void testChatTriggersCreateTaskTool() throws Exception {
        when(llmProvider.generateTextWithTools(anyList(), anyList())).thenAnswer(invocation -> {
            List<FunctionCallback> callbacks = invocation.getArgument(1);
            FunctionCallback createTask = callbacks.stream().filter(c -> c.getName().equals("createTask")).findFirst().orElseThrow();
            String result = createTask.call("{\"title\":\"Prepare quarterly demo\",\"priority\":\"HIGH\"}");
            assertThat(result).contains("Prepare quarterly demo");

            return "Task created with high priority.";
        });

        ChatRequest request = new ChatRequest("Please create a high-priority task to prepare quarterly demo");
        mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Task created with high priority."));

        List<ToolExecution> executions = toolExecutionRepository.findByToolNameAndIsDeletedFalseOrderByCreatedOnDesc("createTask");
        assertThat(executions).hasSize(1);
        assertThat(executions.get(0).getStatus()).isEqualTo("SUCCESS");
    }
}