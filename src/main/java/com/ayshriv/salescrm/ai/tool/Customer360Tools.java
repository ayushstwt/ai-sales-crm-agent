package com.ayshriv.salescrm.ai.tool;

import com.ayshriv.salescrm.activity.entity.Activity;
import com.ayshriv.salescrm.ai.service.ToolExecutionService;
import com.ayshriv.salescrm.common.security.TenantContext;
import com.ayshriv.salescrm.common.security.TenantContextService;
import com.ayshriv.salescrm.company.entity.Company;
import com.ayshriv.salescrm.company.repository.CompanyRepository;
import com.ayshriv.salescrm.contact.entity.Contact;
import com.ayshriv.salescrm.customer.dto.Customer360Dto;
import com.ayshriv.salescrm.customer.dto.GetCustomer360Input;
import com.ayshriv.salescrm.customer.dto.GetCustomer360Output;
import com.ayshriv.salescrm.customer.service.Customer360Service;
import com.ayshriv.salescrm.deal.entity.Deal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI Tools for Customer 360 aggregation and relationship summaries.
 * Flow: AI -> Tool -> Customer360Service -> TenantContext -> Repository.
 */
@Component
public class Customer360Tools {

    private static final Logger LOGGER = LoggerFactory.getLogger(Customer360Tools.class);

    private final Customer360Service customer360Service;
    private final CompanyRepository companyRepository;
    private final ToolExecutionService toolExecutionService;
    private final TenantContextService tenantContextService;
    private final ObjectMapper objectMapper;

    public Customer360Tools(
            Customer360Service customer360Service,
            CompanyRepository companyRepository,
            ToolExecutionService toolExecutionService,
            TenantContextService tenantContextService,
            ObjectMapper objectMapper
    ) {
        this.customer360Service = customer360Service;
        this.companyRepository = companyRepository;
        this.toolExecutionService = toolExecutionService;
        this.tenantContextService = tenantContextService;
        this.objectMapper = objectMapper;
    }

    public GetCustomer360Output getCustomer360(GetCustomer360Input input) {
        LOGGER.info("Customer360Tools >> getCustomer360 called with customerId: {}, companyName: {}",
                input != null ? input.getCustomerId() : null,
                input != null ? input.getCompanyName() : null);

        long startTime = System.currentTimeMillis();
        String inputJson = serialize(input);

        try {
            Long companyId = input != null ? input.getCustomerId() : null;

            if (companyId == null && input != null && input.getCompanyName() != null && !input.getCompanyName().isBlank()) {
                TenantContext context = tenantContextService.getCurrentContext();
                Long orgId = context != null ? context.getOrganizationId() : null;
                String queryName = input.getCompanyName().trim().toLowerCase();

                List<Company> companies = companyRepository.findAll().stream()
                        .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                        .filter(c -> orgId == null || (c.getOrganization() != null && c.getOrganization().getId().equals(orgId)))
                        .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(queryName))
                        .collect(Collectors.toList());

                if (!companies.isEmpty()) {
                    companyId = companies.get(0).getId();
                }
            }

            if (companyId == null) {
                GetCustomer360Output output = new GetCustomer360Output(false, "Customer or company could not be resolved from input.");
                long duration = System.currentTimeMillis() - startTime;
                toolExecutionService.recordExecution("getCustomer360", inputJson, serialize(output), "SUCCESS", duration);
                return output;
            }

            Customer360Dto aggregation = customer360Service.aggregateCustomer360(companyId);

            if (aggregation == null || aggregation.getCompany() == null) {
                GetCustomer360Output output = new GetCustomer360Output(false, "No Customer 360 data found for company ID: " + companyId);
                long duration = System.currentTimeMillis() - startTime;
                toolExecutionService.recordExecution("getCustomer360", inputJson, serialize(output), "SUCCESS", duration);
                return output;
            }

            Company company = aggregation.getCompany();
            GetCustomer360Output output = new GetCustomer360Output();
            output.setSuccess(true);
            output.setCompanyId(company.getId());
            output.setCompanyName(company.getName());
            output.setDomain(company.getDomain());
            output.setIndustry(company.getIndustry());
            output.setContactCount(aggregation.getTotalContacts());
            output.setDealCount(aggregation.getTotalDeals());
            output.setTotalPipelineValue(aggregation.getTotalPipelineValue());
            output.setWonValue(aggregation.getTotalWonValue());
            output.setOpenTasksCount(aggregation.getOpenTasksCount());
            output.setActivityCount(aggregation.getTotalActivitiesCount());

            List<String> contactNames = aggregation.getContacts().stream()
                    .map(c -> c.getFirstName() + " " + (c.getLastName() != null ? c.getLastName() : "") + " (" + (c.getJobTitle() != null ? c.getJobTitle() : "Contact") + ")")
                    .collect(Collectors.toList());
            output.setContactNames(contactNames);

            List<String> dealSummaries = aggregation.getDeals().stream()
                    .map(d -> "'" + d.getTitle() + "' - $" + d.getAmount() + " [" + d.getStatus() + "]")
                    .collect(Collectors.toList());
            output.setDealSummaries(dealSummaries);

            List<String> recentActivities = aggregation.getActivities().stream().limit(8)
                    .map(a -> "[" + a.getType() + "] " + a.getTitle() + (a.getDescription() != null ? ": " + a.getDescription() : ""))
                    .collect(Collectors.toList());
            output.setRecentActivitySummaries(recentActivities);

            List<String> notes = aggregation.getNotes().stream()
                    .map(Activity::getDescription)
                    .collect(Collectors.toList());
            output.setNotes(notes);

            output.setMessage("Successfully aggregated Customer 360 data for " + company.getName());

            long duration = System.currentTimeMillis() - startTime;
            toolExecutionService.recordExecution("getCustomer360", inputJson, serialize(output), "SUCCESS", duration);
            return output;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.error("Customer360Tools >> getCustomer360 failed: {}", e.getMessage(), e);
            toolExecutionService.recordExecution("getCustomer360", inputJson, "Error: " + e.getMessage(), "ERROR", duration);
            return new GetCustomer360Output(false, "Error aggregating Customer 360 data: " + e.getMessage());
        }
    }

    public FunctionCallback getCustomer360FunctionCallback() {
        return FunctionCallbackWrapper.builder((Function<GetCustomer360Input, GetCustomer360Output>) this::getCustomer360)
                .withName("getCustomer360")
                .withDescription("Retrieve comprehensive 360-degree customer profile data for a company (contacts, deals, pipeline value, won revenue, open tasks, recent activities, and notes) to answer questions and generate natural language customer relationship summaries.")
                .withInputType(GetCustomer360Input.class)
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