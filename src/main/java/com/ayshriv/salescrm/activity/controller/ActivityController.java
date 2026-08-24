package com.ayshriv.salescrm.activity.controller;

import com.ayshriv.salescrm.activity.dto.ActivityCreateRequest;
import com.ayshriv.salescrm.activity.dto.ActivitySearchRequest;
import com.ayshriv.salescrm.activity.dto.TimelineItemDto;
import com.ayshriv.salescrm.activity.service.ActivityService;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Resources;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public ResponseEntity<MappingJacksonValue> listActivities(@ModelAttribute ActivitySearchRequest request) {
        ApiStatus status = activityService.listActivities(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "activities", "total"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> viewActivity(@PathVariable Long id) {
        ApiStatus status = activityService.viewActivity(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "activity"));
    }

    @PostMapping
    public ResponseEntity<MappingJacksonValue> createActivity(@Valid @RequestBody ActivityCreateRequest request) {
        ApiStatus status = activityService.createActivity(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "activity"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> deleteActivity(@PathVariable Long id) {
        ApiStatus status = activityService.deleteActivity(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text"));
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<TimelineItemDto>> getTimeline(
            @RequestParam(required = false) Long leadId,
            @RequestParam(required = false) Long contactId,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long dealId
    ) {
        List<TimelineItemDto> timeline = activityService.getCustomerTimeline(leadId, contactId, companyId, dealId);
        return ResponseEntity.ok(timeline);
    }
}