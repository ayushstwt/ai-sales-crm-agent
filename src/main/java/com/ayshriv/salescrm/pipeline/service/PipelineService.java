package com.ayshriv.salescrm.pipeline.service;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.pipeline.dto.PipelineCreateRequest;
import com.ayshriv.salescrm.pipeline.dto.PipelineSearchRequest;
import com.ayshriv.salescrm.pipeline.dto.PipelineStageCreateRequest;
import com.ayshriv.salescrm.pipeline.dto.PipelineStageUpdateRequest;
import com.ayshriv.salescrm.pipeline.dto.PipelineUpdateRequest;

public interface PipelineService {

    ApiStatus listPipelines(PipelineSearchRequest request);

    ApiStatus viewPipeline(Long id);

    ApiStatus createPipeline(PipelineCreateRequest request);

    ApiStatus editPipeline(Long id, PipelineUpdateRequest request);

    ApiStatus deletePipeline(Long id);

    ApiStatus addStage(Long pipelineId, PipelineStageCreateRequest request);

    ApiStatus editStage(Long stageId, PipelineStageUpdateRequest request);

    ApiStatus deleteStage(Long stageId);
}
