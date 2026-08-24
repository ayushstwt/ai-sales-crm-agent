package com.ayshriv.salescrm.lead.repository;

import com.ayshriv.salescrm.lead.entity.Lead;
import com.ayshriv.salescrm.lead.entity.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    Optional<Lead> findByIdAndIsDeletedFalse(Long id);

    Optional<Lead> findByIdAndOrganizationIdAndIsDeletedFalse(Long id, Long organizationId);

    Page<Lead> findByOrganizationIdAndIsDeletedFalse(Long organizationId, Pageable pageable);

    Page<Lead> findByIsDeletedFalse(Pageable pageable);

    Page<Lead> findByOrganizationIdAndStatusAndIsDeletedFalse(Long organizationId, LeadStatus status, Pageable pageable);

    List<Lead> findByOrganizationIdAndStatusAndIsDeletedFalse(Long organizationId, LeadStatus status);

    @Query("""
        SELECT l FROM Lead l 
        WHERE l.organization.id = :organizationId 
          AND l.isDeleted = false 
          AND (:status IS NULL OR l.status = :status)
          AND (:companyName IS NULL OR LOWER(l.companyName) LIKE LOWER(CONCAT('%', :companyName, '%')))
          AND (:email IS NULL OR LOWER(l.email) LIKE LOWER(CONCAT('%', :email, '%')))
          AND (:name IS NULL OR LOWER(l.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(l.lastName) LIKE LOWER(CONCAT('%', :name, '%')))
    """)
    Page<Lead> searchLeads(
            @Param("organizationId") Long organizationId,
            @Param("status") LeadStatus status,
            @Param("companyName") String companyName,
            @Param("email") String email,
            @Param("name") String name,
            Pageable pageable
    );
}