package com.ayshriv.salescrm.lead.controller;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.lead.dto.LeadConvertRequest;
import com.ayshriv.salescrm.lead.dto.LeadCreateRequest;
import com.ayshriv.salescrm.lead.dto.LeadSearchRequest;
import com.ayshriv.salescrm.lead.dto.LeadUpdateRequest;
import com.ayshriv.salescrm.lead.service.LeadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    public ResponseEntity<MappingJacksonValue> listLeads(@ModelAttribute LeadSearchRequest request) {
        ApiStatus status = leadService.listLeads(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "leads", "total"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> viewLead(@PathVariable Long id) {
        ApiStatus status = leadService.viewLead(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "lead"));
    }

    @PostMapping
    public ResponseEntity<MappingJacksonValue> createLead(@Valid @RequestBody LeadCreateRequest request) {
        ApiStatus status = leadService.createLead(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "lead"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> editLead(@PathVariable Long id, @RequestBody LeadUpdateRequest request) {
        ApiStatus status = leadService.editLead(id, request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "lead"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> deleteLead(@PathVariable Long id) {
        ApiStatus status = leadService.deleteLead(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text"));
    }

    @PostMapping("/{id}/convert")
    public ResponseEntity<MappingJacksonValue> convertLead(@PathVariable Long id, @RequestBody(required = false) LeadConvertRequest request) {
        if (request == null) {
            request = new LeadConvertRequest();
        }
        ApiStatus status = leadService.convertLead(id, request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "lead", "contact", "company", "deal"));
    }
}
