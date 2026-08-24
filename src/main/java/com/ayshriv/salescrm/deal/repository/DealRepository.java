package com.ayshriv.salescrm.deal.repository;

import com.ayshriv.salescrm.deal.entity.Deal;
import com.ayshriv.salescrm.deal.entity.DealStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {

    Optional<Deal> findByIdAndIsDeletedFalse(Long id);

    Optional<Deal> findByIdAndOrganizationIdAndIsDeletedFalse(Long id, Long organizationId);

    Page<Deal> findByOrganizationIdAndIsDeletedFalse(Long organizationId, Pageable pageable);

    Page<Deal> findByIsDeletedFalse(Pageable pageable);

    List<Deal> findByCompanyIdAndIsDeletedFalse(Long companyId);

    List<Deal> findByContactIdAndIsDeletedFalse(Long contactId);

    List<Deal> findByOrganizationIdAndStatusAndIsDeletedFalse(Long organizationId, DealStatus status);

    @Query("""
        SELECT d FROM Deal d 
        WHERE d.organization.id = :organizationId 
          AND d.isDeleted = false 
          AND (:status IS NULL OR d.status = :status)
          AND (:title IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:companyId IS NULL OR (d.company IS NOT NULL AND d.company.id = :companyId))
          AND (:contactId IS NULL OR (d.contact IS NOT NULL AND d.contact.id = :contactId))
          AND (:stageId IS NULL OR (d.pipelineStage IS NOT NULL AND d.pipelineStage.id = :stageId))
    """)
    Page<Deal> searchDeals(
            @Param("organizationId") Long organizationId,
            @Param("status") DealStatus status,
            @Param("title") String title,
            @Param("companyId") Long companyId,
            @Param("contactId") Long contactId,
            @Param("stageId") Long stageId,
            Pageable pageable
    );
}