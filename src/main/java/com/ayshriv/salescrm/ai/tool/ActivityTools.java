package com.ayshriv.salescrm.ai.tool;

import com.ayshriv.salescrm.activity.dto.TimelineItemDto;
import com.ayshriv.salescrm.activity.service.ActivityService;
import com.ayshriv.salescrm.ai.service.ToolExecutionService;
import com.ayshriv.salescrm.ai.tool.dto.CustomerTimelineInput;
import com.ayshriv.salescrm.ai.tool.dto.CustomerTimelineOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * AI Tool for Customer 360 Timeline & Activity history.
 * 
 * Enforces master.md §7 Rule #2:
 * Flow: AI → Tool → ActivityService (Application Service) → TenantContext/Authorization → Repository.
 */
@Component
public class ActivityTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityTools.class);

    private final ActivityService activityService;
    private final ToolExecutionService toolExecutionService;
    private final ObjectMapper objectMapper;

    public ActivityTools(
            ActivityService activityService,
            ToolExecutionService toolExecutionService,
            ObjectMapper objectMapper
    ) {
        this.activityService = activityService;
        this.toolExecutionService = toolExecutionService;
        this.objectMapper = objectMapper;
    }

    public CustomerTimelineOutput getCustomerTimeline(CustomerTimelineInput input) {
        LOGGER.info("ActivityTools >> getCustomerTimeline called");
        long startTime = System.currentTimeMillis();
        String inputJson = serialize(input);

        try {
            Long leadId = input != null ? input.getLeadId() : null;
            Long contactId = input != null ? input.getContactId() : null;
            Long companyId = input != null ? input.getCompanyId() : null;
            Long dealId = input != null ? input.getDealId() : null;

            // CRITICAL: Call the Stage 2 Application Service, NEVER repository directly
            List<TimelineItemDto> timeline = activityService.getCustomerTimeline(leadId, contactId, companyId, dealId);

            CustomerTimelineOutput output = new CustomerTimelineOutput(
                    timeline != null ? timeline.size() : 0,
                    timeline,
                    "Customer timeline retrieved successfully."
            );

            long duration = System.currentTimeMillis() - startTime;
            toolExecutionService.recordExecution("getCustomerTimeline", inputJson, serialize(output), "SUCCESS", duration);
            return output;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.error("ActivityTools >> getCustomerTimeline failed: {}", e.getMessage(), e);
            toolExecutionService.recordExecution("getCustomerTimeline", inputJson, "Error: " + e.getMessage(), "ERROR", duration);
            return new CustomerTimelineOutput(0, List.of(), "Error: " + e.getMessage());
        }
    }

    public FunctionCallback getCustomerTimelineFunctionCallback() {
        return FunctionCallbackWrapper.builder((Function<CustomerTimelineInput, CustomerTimelineOutput>) this::getCustomerTimeline)
                .withName("getCustomerTimeline")
                .withDescription("Retrieve chronological activity and audit event timeline for a lead, contact, company, or deal.")
                .withInputType(CustomerTimelineInput.class)
                .build();
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