package com.ayshriv.salescrm.pipeline.repository;

import com.ayshriv.salescrm.pipeline.entity.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PipelineStageRepository extends JpaRepository<PipelineStage, Long> {

    Optional<PipelineStage> findByIdAndIsDeletedFalse(Long id);

    List<PipelineStage> findByPipelineIdAndIsDeletedFalseOrderByOrderIndexAsc(Long pipelineId);
}
