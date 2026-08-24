package com.ayshriv.salescrm.ai.tool;

import com.ayshriv.salescrm.ai.service.ToolExecutionService;
import com.ayshriv.salescrm.ai.tool.dto.DealSummary;
import com.ayshriv.salescrm.ai.tool.dto.GetDealInput;
import com.ayshriv.salescrm.ai.tool.dto.GetDealOutput;
import com.ayshriv.salescrm.ai.tool.dto.SearchDealsInput;
import com.ayshriv.salescrm.ai.tool.dto.SearchDealsOutput;
import com.ayshriv.salescrm.ai.tool.dto.UpdateDealStageInput;
import com.ayshriv.salescrm.ai.tool.dto.UpdateDealStageOutput;
import com.ayshriv.salescrm.audit.entity.AuditSource;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.deal.dto.DealMoveStageRequest;
import com.ayshriv.salescrm.deal.dto.DealSearchRequest;
import com.ayshriv.salescrm.deal.entity.Deal;
import com.ayshriv.salescrm.deal.entity.DealStatus;
import com.ayshriv.salescrm.deal.service.DealService;
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
 * AI Tools for Deal operations.
 * 
 * Enforces master.md §7 Rule #2:
 * Flow: AI → Tool → DealService (Application Service) → TenantContext/Authorization → Repository.
 */
@Component
public class DealTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(DealTools.class);

    private final DealService dealService;
    private final ToolExecutionService toolExecutionService;
    private final ObjectMapper objectMapper;

    public DealTools(
            DealService dealService,
            ToolExecutionService toolExecutionService,
            ObjectMapper objectMapper
    ) {
        this.dealService = dealService;
        this.toolExecutionService = toolExecutionService;
        this.objectMapper = objectMapper;
    }

    public SearchDealsOutput searchDeals(SearchDealsInput input) {
        LOGGER.info("DealTools >> searchDeals called with input: {}", input != null ? input.getTitle() : null);
        long startTime = System.currentTimeMillis();
        String inputJson = serialize(input);

        try {
            DealSearchRequest request = new DealSearchRequest();
            if (input != null) {
                request.setTitle(input.getTitle());
                request.setCompanyId(input.getCompanyId());
                request.setContactId(input.getContactId());
                request.setPipelineStageId(input.getStageId());
                if (input.getStatus() != null && !input.getStatus().isBlank()) {
                    try {
                        request.setStatus(DealStatus.valueOf(input.getStatus().trim().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        LOGGER.warn("DealTools >> Invalid deal status: {}", input.getStatus());
                    }
                }
                if (input.getPageNumber() != null) {
                    request.setPageNumber(input.getPageNumber());
                }
                if (input.getPageSize() != null) {
                    request.setPageSize(input.getPageSize());
                }
            }

            // CRITICAL: Call the Stage 2 Application Service, NEVER repository directly
            ApiStatus status = dealService.listDeals(request);

            List<DealSummary> summaries = new ArrayList<>();
            if (status.getDeals() != null) {
                for (Deal deal : status.getDeals()) {
                    summaries.add(mapToSummary(deal));
                }
            }

            long total = status.getTotal() != null ? status.getTotal() : summaries.size();
            SearchDealsOutput output = new SearchDealsOutput(
                    summaries.size(),
                    total,
                    summaries,
                    status.getText()
            );

            long duration = System.currentTimeMillis() - startTime;
            toolExecutionService.recordExecution("searchDeals", inputJson, serialize(output), "SUCCESS", duration);
            return output;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.error("DealTools >> searchDeals failed: {}", e.getMessage(), e);
            toolExecutionService.recordExecution("searchDeals", inputJson, "Error: " + e.getMessage(), "ERROR", duration);
            return new SearchDealsOutput(0, 0, new ArrayList<>(), "Failed to search deals: " + e.getMessage());
        }
    }

    public GetDealOutput getDeal(GetDealInput input) {
        LOGGER.info("DealTools >> getDeal called for id: {}", input != null ? input.getId() : null);
        long startTime = System.currentTimeMillis();
        String inputJson = serialize(input);

        try {
            if (input == null || input.getId() == null) {
                return new GetDealOutput(false, null, "Deal ID is required.");
            }

            // CRITICAL: Call the Stage 2 Application Service, NEVER repository directly
            ApiStatus status = dealService.viewDeal(input.getId());

            if (status.getDeal() == null) {
                GetDealOutput output = new GetDealOutput(false, null, "Deal not found with ID: " + input.getId());
                long duration = System.currentTimeMillis() - startTime;
                toolExecutionService.recordExecution("getDeal", inputJson, serialize(output), "SUCCESS", duration);
                return output;
            }

            DealSummary summary = mapToSummary(status.getDeal());
            GetDealOutput output = new GetDealOutput(true, summary, "Deal retrieved successfully.");

            long duration = System.currentTimeMillis() - startTime;
            toolExecutionService.recordExecution("getDeal", inputJson, serialize(output), "SUCCESS", duration);
            return output;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.error("DealTools >> getDeal failed: {}", e.getMessage(), e);
            toolExecutionService.recordExecution("getDeal", inputJson, "Error: " + e.getMessage(), "ERROR", duration);
            return new GetDealOutput(false, null, "Error fetching deal: " + e.getMessage());
        }
    }

    public UpdateDealStageOutput updateDealStage(UpdateDealStageInput input) {
        LOGGER.info("DealTools >> updateDealStage called for deal id: {} to stage: {}",
                input != null ? input.getDealId() : null, input != null ? input.getStageId() : null);
        long startTime = System.currentTimeMillis();
        String inputJson = serialize(input);

        try {
            if (input == null || input.getDealId() == null || input.getStageId() == null) {
                return new UpdateDealStageOutput(false, null, null, null, "dealId and stageId are required.");
            }

            DealMoveStageRequest moveRequest = new DealMoveStageRequest();
            moveRequest.setPipelineStageId(input.getStageId());
            if (input.getStatus() != null && !input.getStatus().isBlank()) {
                try {
                    moveRequest.setStatus(DealStatus.valueOf(input.getStatus().trim().toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }

            // CRITICAL: Call the Stage 2 Application Service with AuditSource.AI_AGENT
            ApiStatus status = dealService.moveStage(input.getDealId(), moveRequest, AuditSource.AI_AGENT);

            if (status.getDeal() == null) {
                UpdateDealStageOutput output = new UpdateDealStageOutput(false, input.getDealId(), null, null, status.getText());
                long duration = System.currentTimeMillis() - startTime;
                toolExecutionService.recordExecution("updateDealStage", inputJson, serialize(output), "FAILURE", duration);
                return output;
            }

            Deal updated = status.getDeal();
            String stageName = updated.getPipelineStage() != null ? updated.getPipelineStage().getName() : "Stage " + input.getStageId();
            UpdateDealStageOutput output = new UpdateDealStageOutput(
                    true,
                    updated.getId(),
                    stageName,
                    updated.getStatus() != null ? updated.getStatus().name() : null,
                    "Deal stage moved to " + stageName + " successfully."
            );

            long duration = System.currentTimeMillis() - startTime;
            toolExecutionService.recordExecution("updateDealStage", inputJson, serialize(output), "SUCCESS", duration);
            return output;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.error("DealTools >> updateDealStage failed: {}", e.getMessage(), e);
            toolExecutionService.recordExecution("updateDealStage", inputJson, "Error: " + e.getMessage(), "ERROR", duration);
            return new UpdateDealStageOutput(false, input != null ? input.getDealId() : null, null, null, "Error: " + e.getMessage());
        }
    }

    public FunctionCallback searchDealsFunctionCallback() {
        return FunctionCallbackWrapper.builder((Function<SearchDealsInput, SearchDealsOutput>) this::searchDeals)
                .withName("searchDeals")
                .withDescription("Search and list CRM deals matching title, company, contact, pipeline stage, or status (OPEN, WON, LOST).")
                .withInputType(SearchDealsInput.class)
                .build();
    }

    public FunctionCallback getDealFunctionCallback() {
        return FunctionCallbackWrapper.builder((Function<GetDealInput, GetDealOutput>) this::getDeal)
                .withName("getDeal")
                .withDescription("Retrieve detailed information about a specific CRM deal by its ID.")
                .withInputType(GetDealInput.class)
                .build();
    }

    public FunctionCallback updateDealStageFunctionCallback() {
        return FunctionCallbackWrapper.builder((Function<UpdateDealStageInput, UpdateDealStageOutput>) this::updateDealStage)
                .withName("updateDealStage")
                .withDescription("Update the pipeline stage of an existing deal. Pass dealId and the target stageId.")
                .withInputType(UpdateDealStageInput.class)
                .build();
    }

    private DealSummary mapToSummary(Deal deal) {
        return new DealSummary(
                deal.getId(),
                deal.getTitle(),
                deal.getAmount(),
                deal.getCurrency(),
                deal.getStatus() != null ? deal.getStatus().name() : null,
                deal.getPipelineStage() != null ? deal.getPipelineStage().getId() : null,
                deal.getPipelineStage() != null ? deal.getPipelineStage().getName() : null,
                deal.getCompany() != null ? deal.getCompany().getName() : null,
                deal.getContact() != null ? (deal.getContact().getFirstName() + " " + (deal.getContact().getLastName() != null ? deal.getContact().getLastName() : "")) : null
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