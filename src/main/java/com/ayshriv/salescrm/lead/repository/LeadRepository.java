package com.ayshriv.salescrm.lead.repository;

import com.ayshriv.salescrm.lead.entity.Lead;
import com.ayshriv.salescrm.lead.entity.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
