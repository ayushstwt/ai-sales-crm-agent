package com.ayshriv.salescrm.ai.service.impl;

import com.ayshriv.salescrm.ai.entity.Conversation;
import com.ayshriv.salescrm.ai.entity.ToolExecution;
import com.ayshriv.salescrm.ai.repository.ConversationRepository;
import com.ayshriv.salescrm.ai.repository.ToolExecutionRepository;
import com.ayshriv.salescrm.ai.service.ToolExecutionService;
import com.ayshriv.salescrm.common.security.TenantContext;
import com.ayshriv.salescrm.common.security.TenantContextService;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class ToolExecutionServiceImpl implements ToolExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolExecutionServiceImpl.class);

    private final ToolExecutionRepository toolExecutionRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final TenantContextService tenantContextService;

    public ToolExecutionServiceImpl(
            ToolExecutionRepository toolExecutionRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            ConversationRepository conversationRepository,
            TenantContextService tenantContextService
    ) {
        this.toolExecutionRepository = toolExecutionRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.tenantContextService = tenantContextService;
    }

    @Override
    @Transactional
    public ToolExecution recordExecution(String toolName, String arguments, String result, String status, Long executionTimeMs) {
        return recordExecution(null, toolName, arguments, result, status, executionTimeMs);
    }

    @Override
    @Transactional
    public ToolExecution recordExecution(Long conversationId, String toolName, String arguments, String result, String status, Long executionTimeMs) {
        try {
            TenantContext context = tenantContextService.getCurrentContext();
            Organization org = null;
            if (context != null && context.getOrganizationId() != null) {
                org = organizationRepository.findByIdAndIsDeletedFalse(context.getOrganizationId()).orElse(null);
            }
            if (org == null) {
                org = organizationRepository.findAll().stream()
                        .filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
                        .findFirst()
                        .orElse(null);
            }

            if (org == null) {
                LOGGER.warn("ToolExecutionService >> Skipped recording tool execution: no organization available");
                return null;
            }

            User user = null;
            if (context != null && context.getUserId() != null) {
                user = userRepository.findByIdAndIsDeletedFalse(context.getUserId()).orElse(null);
            }

            Conversation conversation = null;
            if (conversationId != null) {
                conversation = conversationRepository.findByIdAndIsDeletedFalse(conversationId).orElse(null);
            }

            ToolExecution execution = new ToolExecution(
                    org,
                    user,
                    conversation,
                    toolName,
                    arguments,
                    result,
                    status != null ? status : "SUCCESS",
                    executionTimeMs
            );

            ToolExecution saved = toolExecutionRepository.save(execution);
            LOGGER.info("ToolExecution logged >> ID: {}, Tool: {}, Org: {}, Status: {}, Duration: {}ms",
                    saved.getId(), toolName, org.getId(), status, executionTimeMs);
            return saved;

        } catch (Exception e) {
            LOGGER.error("ToolExecutionService >> recordExecution error: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ToolExecution> getExecutionsForConversation(Long conversationId) {
        if (conversationId == null) {
            return Collections.emptyList();
        }
        return toolExecutionRepository.findByConversationIdAndIsDeletedFalseOrderByCreatedOnAsc(conversationId);
    }
}