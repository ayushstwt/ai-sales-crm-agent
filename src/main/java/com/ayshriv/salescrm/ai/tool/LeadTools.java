package com.ayshriv.salescrm.ai.tool;

import com.ayshriv.salescrm.ai.service.ToolExecutionService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
    private final ObjectMapper objectMapper;

    public LeadTools(
            LeadService leadService,
            ToolExecutionService toolExecutionService,
            ObjectMapper objectMapper
    ) {
        this.leadService = leadService;
        this.toolExecutionService = toolExecutionService;
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