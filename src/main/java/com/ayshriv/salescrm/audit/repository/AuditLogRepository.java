package com.ayshriv.salescrm.audit.repository;

import com.ayshriv.salescrm.audit.entity.AuditLog;
import com.ayshriv.salescrm.audit.entity.AuditSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Optional<AuditLog> findByIdAndIsDeletedFalse(Long id);

    Optional<AuditLog> findByIdAndOrganizationIdAndIsDeletedFalse(Long id, Long organizationId);

    Page<AuditLog> findByOrganizationIdAndIsDeletedFalse(Long organizationId, Pageable pageable);

    Page<AuditLog> findByOrganizationIdAndResourceTypeAndIsDeletedFalse(Long organizationId, String resourceType, Pageable pageable);

    Page<AuditLog> findByOrganizationIdAndSourceAndIsDeletedFalse(Long organizationId, AuditSource source, Pageable pageable);

    Page<AuditLog> findByIsDeletedFalse(Pageable pageable);
}
