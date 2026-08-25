package com.ayshriv.salescrm.ai.tool;

import com.ayshriv.salescrm.ai.context.ConversationContextHolder;
import com.ayshriv.salescrm.ai.dto.PendingActionPayload;
import com.ayshriv.salescrm.ai.entity.PendingActionStatus;
import com.ayshriv.salescrm.ai.repository.ConversationRepository;
import com.ayshriv.salescrm.ai.service.ToolExecutionService;
import com.ayshriv.salescrm.ai.tool.dto.BulkDeleteLeadsInput;
import com.ayshriv.salescrm.ai.tool.dto.BulkDeleteLeadsOutput;
import com.ayshriv.salescrm.ai.tool.dto.GetLeadInput;
import com.ayshriv.salescrm.ai.tool.dto.GetLeadOutput;
import com.ayshriv.salescrm.ai.tool.dto.LeadSummary;
import com.ayshriv.salescrm.ai.tool.dto.SearchLeadsInput;
import com.ayshriv.salescrm.ai.tool.dto.SearchLeadsOutput;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.lead.dto.LeadSearchRequest;
import com.ayshriv.salescrm.lead.entity.Lead;
import com.ayshriv.salescrm.lead.entity.LeadStatus;
import com.ayshriv.salescrm.lead.service.LeadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI Tools for Lead operations.
 * 
 * Enforces master.md §7 Rule #2:
 * Flow: AI → Tool → LeadService (Application Service) → TenantContext/Authorization → Repository.
 */
@Component
public class LeadTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeadTools.class);

    private final LeadService leadService;
    private final ToolExecutionService toolExecutionService;
    private final ConversationRepository conversationRepository;
    private final ObjectMapper objectMapper;

    public LeadTools(
            LeadService leadService,
            ToolExecutionService toolExecutionService,
            ConversationRepository conversationRepository,
            ObjectMapper objectMapper
    ) {
        this.leadService = leadService;
        this.toolExecutionService = toolExecutionService;
        this.conversationRepository = conversationRepository;
        this.objectMapper = objectMapper;
    }

    public SearchLeadsOutput searchLeads(SearchLeadsInput input) {
        LOGGER.info("LeadTools >> searchLeads called with input: {}", input != null ? input.getName() : null);
        long startTime = System.currentTimeMillis();
        String inputJson = serialize(input);

        try {
            LeadSearchRequest searchRequest = new LeadSearchRequest();
            if (input != null) {
                searchRequest.setName(input.getName());
                searchRequest.setEmail(input.getEmail());
                searchRequest.setCompanyName(input.getCompanyName());
                if (input.getStatus() != null && !input.getStatus().isBlank()) {
                    try {
                        searchRequest.setStatus(LeadStatus.valueOf(input.getStatus().trim().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        LOGGER.warn("LeadTools >> Invalid lead status passed: {}", input.getStatus());
                    }
                }
                if (input.getPageNumber() != null) {
                    searchRequest.setPageNumber(input.getPageNumber());
                }
                if (input.getPageSize() != null) {
                    searchRequest.setPageSize(input.getPageSize());
                }
            }

            // CRITICAL: Call the Stage 2 Application Service, NEVER repository directly
            ApiStatus status = leadService.listLeads(searchRequest);

            List<LeadSummary> leadSummaries = new ArrayList<>();
            if (status.getLeads() != null) {
                for (Lead lead : status.getLeads()) {
                    leadSummaries.add(mapToSummary(lead));
                }
            }

            long total = status.getTotal() != null ? status.getTotal() : leadSummaries.size();
            SearchLeadsOutput output = new SearchLeadsOutput(
                    leadSummaries.size(),
                    total,
                    leadSummaries,
                    status.getText()
            );

            long duration = System.currentTimeMillis() - startTime;
            toolExecutionService.recordExecution(
                    "searchLeads",
                    inputJson,
                    serialize(output),
                    "SUCCESS",
                    duration
            );

            return output;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.error("LeadTools >> searchLeads failed: {}", e.getMessage(), e);
            toolExecutionService.recordExecution(
                    "searchLeads",
                    inputJson,
                    "Error: " + e.getMessage(),
                    "ERROR",
                    duration
            );
            return new SearchLeadsOutput(0, 0, new ArrayList<>(), "Failed to search leads: " + e.getMessage());
        }
    }

    public GetLeadOutput getLead(GetLeadInput input) {
        LOGGER.info("LeadTools >> getLead called for id: {}", input != null ? input.getId() : null);
        long startTime = System.currentTimeMillis();
        String inputJson = serialize(input);

        try {
            if (input == null || input.getId() == null) {
                return new GetLeadOutput(false, null, "Lead ID is required.");
            }

            // CRITICAL: Call the Stage 2 Application Service, NEVER repository directly
            ApiStatus status = leadService.viewLead(input.getId());

            if (status.getLead() == null) {
                GetLeadOutput output = new GetLeadOutput(false, null, "Lead not found with ID: " + input.getId());
                long duration = System.currentTimeMillis() - startTime;
                toolExecutionService.recordExecution("getLead", inputJson, serialize(output), "SUCCESS", duration);
                return output;
            }

            LeadSummary summary = mapToSummary(status.getLead());
            GetLeadOutput output = new GetLeadOutput(true, summary, "Lead found successfully.");

            long duration = System.currentTimeMillis() - startTime;
            toolExecutionService.recordExecution("getLead", inputJson, serialize(output), "SUCCESS", duration);
            return output;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.error("LeadTools >> getLead failed: {}", e.getMessage(), e);
            toolExecutionService.recordExecution("getLead", inputJson, "Error: " + e.getMessage(), "ERROR", duration);
            return new GetLeadOutput(false, null, "Error fetching lead: " + e.getMessage());
        }
    }

    public FunctionCallback searchLeadsFunctionCallback() {
        return FunctionCallbackWrapper.builder((Function<SearchLeadsInput, SearchLeadsOutput>) this::searchLeads)
                .withName("searchLeads")
                .withDescription("Search and list CRM leads matching query criteria such as name, email, company name, or lead status (NEW, CONTACTED, QUALIFIED, UNQUALIFIED, CONVERTED, LOST).")
                .withInputType(SearchLeadsInput.class)
                .build();
    }

    public FunctionCallback getLeadFunctionCallback() {
        return FunctionCallbackWrapper.builder((Function<GetLeadInput, GetLeadOutput>) this::getLead)
                .withName("getLead")
                .withDescription("Retrieve detailed information about a specific CRM lead by its ID.")
                .withInputType(GetLeadInput.class)
                .build();
    }

    public BulkDeleteLeadsOutput requestBulkDeleteLeads(BulkDeleteLeadsInput input) {
        LOGGER.info("LeadTools >> requestBulkDeleteLeads called with input status: {}, company: {}, ids: {}",
                input != null ? input.getStatus() : null,
                input != null ? input.getCompanyName() : null,
                input != null ? input.getLeadIds() : null);
        long startTime = System.currentTimeMillis();
        String inputJson = serialize(input);

        try {
            List<Lead> leadsToDelete = new ArrayList<>();

            if (input != null && input.getLeadIds() != null && !input.getLeadIds().isEmpty()) {
                for (Long id : input.getLeadIds()) {
                    ApiStatus status = leadService.viewLead(id);
                    if (status.getLead() != null && !Boolean.TRUE.equals(status.getLead().getIsDeleted())) {
                        leadsToDelete.add(status.getLead());
                    }
                }
            } else {
                LeadSearchRequest searchRequest = new LeadSearchRequest();
                if (input != null) {
                    searchRequest.setName(input.getName());
                    searchRequest.setEmail(input.getEmail());
                    searchRequest.setCompanyName(input.getCompanyName());
                    if (input.getStatus() != null && !input.getStatus().isBlank()) {
                        try {
                            searchRequest.setStatus(LeadStatus.valueOf(input.getStatus().trim().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            LOGGER.warn("LeadTools >> Invalid lead status passed: {}", input.getStatus());
                        }
                    }
                    searchRequest.setPageSize(100);
                }
                ApiStatus status = leadService.listLeads(searchRequest);
                if (status.getLeads() != null) {
                    leadsToDelete.addAll(status.getLeads());
                }
            }

            if (leadsToDelete.isEmpty()) {
                BulkDeleteLeadsOutput output = new BulkDeleteLeadsOutput(
                        false,
                        0,
                        List.of(),
                        List.of(),
                        "No matching active leads found to delete."
                );
                long duration = System.currentTimeMillis() - startTime;
                toolExecutionService.recordExecution("requestBulkDeleteLeads", inputJson, serialize(output), "SUCCESS", duration);
                return output;
            }

            List<Long> leadIds = leadsToDelete.stream().map(Lead::getId).collect(Collectors.toList());
            List<LeadSummary> previews = leadsToDelete.stream().map(this::mapToSummary).collect(Collectors.toList());

            Long conversationId = ConversationContextHolder.getConversationId();
            if (conversationId != null) {
                conversationRepository.findByIdAndIsDeletedFalse(conversationId).ifPresent(conversation -> {
                    String description = "Delete " + leadIds.size() + " leads: " +
                            leadsToDelete.stream().map(l -> (l.getFirstName() != null ? l.getFirstName() + " " : "") +
                                            (l.getLastName() != null ? l.getLastName() : "") + " (ID: " + l.getId() + ")")
                                    .collect(Collectors.joining(", "));
                    PendingActionPayload payload = new PendingActionPayload("BULK_DELETE_LEADS", leadIds.size(), leadIds, description);
                    conversation.setPendingActionStatus(PendingActionStatus.PENDING);
                    conversation.setPendingActionType("BULK_DELETE_LEADS");
                    conversation.setPendingActionPayload(serialize(payload));
                    conversation.setPendingActionDescription(description);
                    conversation.setPendingActionCreatedOn(LocalDateTime.now());
                    conversationRepository.save(conversation);
                    LOGGER.info("LeadTools >> Staged pending bulk delete of {} leads on conversation {}", leadIds.size(), conversationId);
                });
            }

            String msg = "DESTRUCTIVE ACTION WARNING: Found " + leadIds.size() + " leads matching deletion criteria. " +
                    "Explicit two-step confirmation required. Do NOT delete yet. " +
                    "Ask the user: 'I found " + leadIds.size() + " leads matching your request. Are you sure you want to delete them? Reply with confirm/yes to proceed or cancel/no to abort.'";

            BulkDeleteLeadsOutput output = new BulkDeleteLeadsOutput(
                    true,
                    leadIds.size(),
                    leadIds,
                    previews,
                    msg
            );

            long duration = System.currentTimeMillis() - startTime;
            toolExecutionService.recordExecution("requestBulkDeleteLeads", inputJson, serialize(output), "SUCCESS", duration);
            return output;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.error("LeadTools >> requestBulkDeleteLeads failed: {}", e.getMessage(), e);
            toolExecutionService.recordExecution("requestBulkDeleteLeads", inputJson, "Error: " + e.getMessage(), "ERROR", duration);
            return new BulkDeleteLeadsOutput(false, 0, List.of(), List.of(), "Error requesting bulk delete: " + e.getMessage());
        }
    }

    public FunctionCallback requestBulkDeleteLeadsFunctionCallback() {
        return FunctionCallbackWrapper.builder((Function<BulkDeleteLeadsInput, BulkDeleteLeadsOutput>) this::requestBulkDeleteLeads)
                .withName("requestBulkDeleteLeads")
                .withDescription("Stage a destructive bulk deletion request for leads matching criteria (status, company name, name, or IDs). IMPORTANT: This does NOT delete immediately. It previews the count and records pending confirmation state.")
                .withInputType(BulkDeleteLeadsInput.class)
                .build();
    }

    private LeadSummary mapToSummary(Lead lead) {
        return new LeadSummary(
                lead.getId(),
                lead.getFirstName(),
                lead.getLastName(),
                lead.getEmail(),
                lead.getPhone(),
                lead.getCompanyName(),
                lead.getJobTitle(),
                lead.getStatus() != null ? lead.getStatus().name() : null,
                lead.getSource()
        );
    }

    private String serialize(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}