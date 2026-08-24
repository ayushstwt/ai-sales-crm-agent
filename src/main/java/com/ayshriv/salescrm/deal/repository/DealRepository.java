package com.ayshriv.salescrm.deal.repository;

import com.ayshriv.salescrm.deal.entity.Deal;
import com.ayshriv.salescrm.deal.entity.DealStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
