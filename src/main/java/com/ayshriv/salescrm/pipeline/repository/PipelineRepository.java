package com.ayshriv.salescrm.pipeline.repository;

import com.ayshriv.salescrm.pipeline.entity.Pipeline;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Long> {

    Optional<Pipeline> findByIdAndIsDeletedFalse(Long id);

    Optional<Pipeline> findByIdAndOrganizationIdAndIsDeletedFalse(Long id, Long organizationId);

    Optional<Pipeline> findByOrganizationIdAndIsDefaultTrueAndIsDeletedFalse(Long organizationId);

    Page<Pipeline> findByOrganizationIdAndIsDeletedFalse(Long organizationId, Pageable pageable);

    Page<Pipeline> findByIsDeletedFalse(Pageable pageable);
}
