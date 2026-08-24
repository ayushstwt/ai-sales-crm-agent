package com.ayshriv.salescrm.pipeline.controller;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.pipeline.dto.PipelineCreateRequest;
import com.ayshriv.salescrm.pipeline.dto.PipelineSearchRequest;
import com.ayshriv.salescrm.pipeline.dto.PipelineStageCreateRequest;
import com.ayshriv.salescrm.pipeline.dto.PipelineStageUpdateRequest;
import com.ayshriv.salescrm.pipeline.dto.PipelineUpdateRequest;
import com.ayshriv.salescrm.pipeline.service.PipelineService;
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
@RequestMapping("/pipelines")
public class PipelineController {

    private final PipelineService pipelineService;

    public PipelineController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @GetMapping
    public ResponseEntity<MappingJacksonValue> listPipelines(@ModelAttribute PipelineSearchRequest request) {
        ApiStatus status = pipelineService.listPipelines(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "pipelines", "total"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> viewPipeline(@PathVariable Long id) {
        ApiStatus status = pipelineService.viewPipeline(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "pipeline"));
    }

    @PostMapping
    public ResponseEntity<MappingJacksonValue> createPipeline(@Valid @RequestBody PipelineCreateRequest request) {
        ApiStatus status = pipelineService.createPipeline(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "pipeline"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> editPipeline(@PathVariable Long id, @RequestBody PipelineUpdateRequest request) {
        ApiStatus status = pipelineService.editPipeline(id, request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "pipeline"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> deletePipeline(@PathVariable Long id) {
        ApiStatus status = pipelineService.deletePipeline(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text"));
    }

    @PostMapping("/{pipelineId}/stages")
    public ResponseEntity<MappingJacksonValue> addStage(@PathVariable Long pipelineId, @Valid @RequestBody PipelineStageCreateRequest request) {
        ApiStatus status = pipelineService.addStage(pipelineId, request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "pipelineStage"));
    }

    @PutMapping("/stages/{stageId}")
    public ResponseEntity<MappingJacksonValue> editStage(@PathVariable Long stageId, @RequestBody PipelineStageUpdateRequest request) {
        ApiStatus status = pipelineService.editStage(stageId, request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "pipelineStage"));
    }

    @DeleteMapping("/stages/{stageId}")
    public ResponseEntity<MappingJacksonValue> deleteStage(@PathVariable Long stageId) {
        ApiStatus status = pipelineService.deleteStage(stageId);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text"));
    }
}
