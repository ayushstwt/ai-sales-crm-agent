package com.ayshriv.salescrm.ai.service.impl;

import com.ayshriv.salescrm.ai.dto.ChatMessageDto;
import com.ayshriv.salescrm.ai.dto.ChatRequest;
import com.ayshriv.salescrm.ai.dto.ChatResponse;
import com.ayshriv.salescrm.ai.dto.ToolCallDto;
import com.ayshriv.salescrm.ai.entity.Conversation;
import com.ayshriv.salescrm.ai.entity.ConversationMessage;
import com.ayshriv.salescrm.ai.entity.MessageRole;
import com.ayshriv.salescrm.ai.entity.ToolExecution;
import com.ayshriv.salescrm.ai.repository.ConversationMessageRepository;
import com.ayshriv.salescrm.ai.repository.ConversationRepository;
import com.ayshriv.salescrm.ai.service.AiChatService;
import com.ayshriv.salescrm.ai.service.LLMProvider;
import com.ayshriv.salescrm.ai.service.ToolExecutionService;
import com.ayshriv.salescrm.ai.tool.ActivityTools;
import com.ayshriv.salescrm.ai.tool.DealTools;
import com.ayshriv.salescrm.ai.tool.LeadTools;
import com.ayshriv.salescrm.ai.tool.TaskTools;
import com.ayshriv.salescrm.common.security.TenantContext;
import com.ayshriv.salescrm.common.security.TenantContextService;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiChatServiceImpl implements AiChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiChatServiceImpl.class);

    private final LLMProvider llmProvider;
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final TenantContextService tenantContextService;
    private final LeadTools leadTools;
    private final DealTools dealTools;
    private final TaskTools taskTools;
    private final ActivityTools activityTools;
    private final ToolExecutionService toolExecutionService;

    public AiChatServiceImpl(
            LLMProvider llmProvider,
            ConversationRepository conversationRepository,
            ConversationMessageRepository conversationMessageRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            TenantContextService tenantContextService,
            LeadTools leadTools,
            DealTools dealTools,
            TaskTools taskTools,
            ActivityTools activityTools,
            ToolExecutionService toolExecutionService
    ) {
        this.llmProvider = llmProvider;
        this.conversationRepository = conversationRepository;
        this.conversationMessageRepository = conversationMessageRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.tenantContextService = tenantContextService;
        this.leadTools = leadTools;
        this.dealTools = dealTools;
        this.taskTools = taskTools;
        this.activityTools = activityTools;
        this.toolExecutionService = toolExecutionService;
    }

    @Override
    public String getRawCompletion(String prompt) {
        LOGGER.info("AiChatService >> getRawCompletion called with prompt: {}", prompt);
        return llmProvider.generateText(prompt);
    }

    @Override
    @Transactional
    public ChatResponse chat(ChatRequest request) {
        LOGGER.info("AiChatService >> chat called for conversationId: {}", request.getConversationId());

        TenantContext context = tenantContextService.getCurrentContext();
        Organization organization = resolveOrganization(context);
        User user = resolveUser(context);

        Conversation conversation;
        if (request.getConversationId() != null) {
            conversation = conversationRepository.findByIdAndOrganizationIdAndIsDeletedFalse(
                    request.getConversationId(), organization.getId()
            ).orElseThrow(() -> new IllegalArgumentException("Conversation not found with id: " + request.getConversationId()));
        } else {
            String title = generateTitle(request.getMessage());
            conversation = new Conversation(organization, user, title);
            conversation = conversationRepository.save(conversation);
            LOGGER.info("AiChatService >> Created new conversation with ID: {}", conversation.getId());
        }

        // 1. Save user message
        ConversationMessage userMsg = new ConversationMessage(
                conversation,
                organization,
                MessageRole.USER,
                request.getMessage()
        );
        conversationMessageRepository.save(userMsg);

        // 2. Fetch all messages in conversation for context
        List<ConversationMessage> history = conversationMessageRepository
                .findByConversationIdAndOrganizationIdAndIsDeletedFalseOrderByCreatedOnAsc(
                        conversation.getId(), organization.getId()
                );

        List<Message> springAiMessages = new ArrayList<>();
        for (ConversationMessage msg : history) {
            if (msg.getRole() == MessageRole.USER) {
                springAiMessages.add(new UserMessage(msg.getContent()));
            } else if (msg.getRole() == MessageRole.ASSISTANT) {
                springAiMessages.add(new AssistantMessage(msg.getContent()));
            } else if (msg.getRole() == MessageRole.SYSTEM) {
                springAiMessages.add(new SystemMessage(msg.getContent()));
            }
        }

        // 3. Register tools (read-only and write tools) and call LLM
        List<FunctionCallback> toolCallbacks = List.of(
                leadTools.searchLeadsFunctionCallback(),
                leadTools.getLeadFunctionCallback(),
                dealTools.searchDealsFunctionCallback(),
                dealTools.getDealFunctionCallback(),
                dealTools.updateDealStageFunctionCallback(),
                taskTools.createTaskFunctionCallback(),
                activityTools.getCustomerTimelineFunctionCallback()
        );
        String responseText = llmProvider.generateTextWithTools(springAiMessages, toolCallbacks);

        // 4. Save assistant response
        ConversationMessage assistantMsg = new ConversationMessage(
                conversation,
                organization,
                MessageRole.ASSISTANT,
                responseText != null ? responseText : ""
        );
        conversationMessageRepository.save(assistantMsg);

        // 5. Gather tool executions for response DTO
        List<ToolExecution> executions = toolExecutionService.getExecutionsForConversation(conversation.getId());
        List<ToolCallDto> toolCallDtos = new ArrayList<>();
        for (ToolExecution exec : executions) {
            toolCallDtos.add(new ToolCallDto(exec.getToolName(), exec.getArguments(), exec.getResult()));
        }

        return new ChatResponse(
                conversation.getId(),
                assistantMsg.getContent(),
                MessageRole.ASSISTANT.name(),
                toolCallDtos,
                assistantMsg.getCreatedOn() != null ? assistantMsg.getCreatedOn() : LocalDateTime.now()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getConversationMessages(Long conversationId) {
        LOGGER.info("AiChatService >> getConversationMessages called for conversationId: {}", conversationId);
        TenantContext context = tenantContextService.getCurrentContext();
        Organization organization = resolveOrganization(context);

        List<ConversationMessage> messages = conversationMessageRepository
                .findByConversationIdAndOrganizationIdAndIsDeletedFalseOrderByCreatedOnAsc(
                        conversationId, organization.getId()
                );

        return messages.stream()
                .map(m -> new ChatMessageDto(m.getId(), conversationId, m.getRole(), m.getContent(), m.getCreatedOn()))
                .collect(Collectors.toList());
    }

    private Organization resolveOrganization(TenantContext context) {
        if (context != null && context.getOrganizationId() != null) {
            return organizationRepository.findByIdAndIsDeletedFalse(context.getOrganizationId())
                    .orElseThrow(() -> new IllegalStateException("Organization not found for id: " + context.getOrganizationId()));
        }
        return organizationRepository.findAll().stream()
                .filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active organization found"));
    }

    private User resolveUser(TenantContext context) {
        if (context != null && context.getUserId() != null) {
            return userRepository.findByIdAndIsDeletedFalse(context.getUserId()).orElse(null);
        }
        return userRepository.findAll().stream()
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .findFirst()
                .orElse(null);
    }

    private String generateTitle(String message) {
        if (message == null || message.isBlank()) {
            return "New Conversation";
        }
        String trimmed = message.trim().replaceAll("\\s+", " ");
        if (trimmed.length() <= 40) {
            return trimmed;
        }
        return trimmed.substring(0, 37) + "...";
    }
}