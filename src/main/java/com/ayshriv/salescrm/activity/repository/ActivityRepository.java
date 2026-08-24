package com.ayshriv.salescrm.activity.repository;

import com.ayshriv.salescrm.activity.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Optional<Activity> findByIdAndIsDeletedFalse(Long id);

    Optional<Activity> findByIdAndOrganizationIdAndIsDeletedFalse(Long id, Long organizationId);

    Page<Activity> findByOrganizationIdAndIsDeletedFalse(Long organizationId, Pageable pageable);

    List<Activity> findByOrganizationIdAndCompanyIdAndIsDeletedFalseOrderByActivityDateDesc(Long organizationId, Long companyId);

    List<Activity> findByOrganizationIdAndContactIdAndIsDeletedFalseOrderByActivityDateDesc(Long organizationId, Long contactId);

    List<Activity> findByOrganizationIdAndLeadIdAndIsDeletedFalseOrderByActivityDateDesc(Long organizationId, Long leadId);

    List<Activity> findByOrganizationIdAndDealIdAndIsDeletedFalseOrderByActivityDateDesc(Long organizationId, Long dealId);

    Page<Activity> findByIsDeletedFalse(Pageable pageable);
}