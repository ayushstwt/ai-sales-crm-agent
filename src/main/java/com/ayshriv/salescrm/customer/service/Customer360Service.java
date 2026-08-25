package com.ayshriv.salescrm.customer.service;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.customer.dto.Customer360Dto;
import com.ayshriv.salescrm.customer.dto.Customer360SummaryDto;

public interface Customer360Service {

    /**
     * Stage 4: Pure CRM Customer 360 aggregation.
     * Aggregates Company + Contacts + Leads + Deals + Notes + Activities + Tasks into one ApiStatus payload.
     */
    ApiStatus getCustomer360(Long customerId);

    /**
     * Helper method to return raw Customer360Dto aggregation for tools and services.
     */
    Customer360Dto aggregateCustomer360(Long customerId);

    /**
     * Stage 5.10: Customer 360 AI summary using the Stage 4 aggregation as context.
     */
    Customer360SummaryDto getCustomer360AiSummary(Long customerId);
}
