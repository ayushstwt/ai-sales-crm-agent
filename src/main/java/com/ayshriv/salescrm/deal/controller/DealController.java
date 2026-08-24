package com.ayshriv.salescrm.deal.controller;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.deal.dto.DealCreateRequest;
import com.ayshriv.salescrm.deal.dto.DealMoveStageRequest;
import com.ayshriv.salescrm.deal.dto.DealSearchRequest;
import com.ayshriv.salescrm.deal.dto.DealUpdateRequest;
import com.ayshriv.salescrm.deal.service.DealService;
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
@RequestMapping("/deals")
public class DealController {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @GetMapping
    public ResponseEntity<MappingJacksonValue> listDeals(@ModelAttribute DealSearchRequest request) {
        ApiStatus status = dealService.listDeals(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "deals", "total"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> viewDeal(@PathVariable Long id) {
        ApiStatus status = dealService.viewDeal(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "deal"));
    }

    @PostMapping
    public ResponseEntity<MappingJacksonValue> createDeal(@Valid @RequestBody DealCreateRequest request) {
        ApiStatus status = dealService.createDeal(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "deal"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> editDeal(@PathVariable Long id, @RequestBody DealUpdateRequest request) {
        ApiStatus status = dealService.editDeal(id, request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "deal"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> deleteDeal(@PathVariable Long id) {
        ApiStatus status = dealService.deleteDeal(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text"));
    }

    @PostMapping("/{id}/move-stage")
    public ResponseEntity<MappingJacksonValue> moveStage(@PathVariable Long id, @Valid @RequestBody DealMoveStageRequest request) {
        ApiStatus status = dealService.moveStage(id, request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "deal"));
    }
}
