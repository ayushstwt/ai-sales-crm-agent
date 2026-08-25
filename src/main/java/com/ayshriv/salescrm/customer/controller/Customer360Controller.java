package com.ayshriv.salescrm.customer.controller;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.customer.dto.Customer360SummaryDto;
import com.ayshriv.salescrm.customer.service.Customer360Service;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class Customer360Controller {

    private final Customer360Service customer360Service;

    public Customer360Controller(Customer360Service customer360Service) {
        this.customer360Service = customer360Service;
    }

    /**
     * Stage 4: Pure CRM Customer 360 aggregation endpoint.
     */
    @GetMapping("/{id}/360")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MappingJacksonValue> getCustomer360(@PathVariable("id") Long id) {
        ApiStatus status = customer360Service.getCustomer360(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "customer360", "company", "contacts", "deals", "leads", "tasks", "activities"));
    }

    /**
     * Stage 5.10: Customer 360 AI summary endpoint.
     */
    @GetMapping("/{id}/360/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Customer360SummaryDto> getCustomer360AiSummary(@PathVariable("id") Long id) {
        Customer360SummaryDto summary = customer360Service.getCustomer360AiSummary(id);
        return ResponseEntity.ok(summary);
    }
}
