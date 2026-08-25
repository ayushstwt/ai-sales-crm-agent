package com.ayshriv.salescrm.document.repository;

import com.ayshriv.salescrm.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByIdAndIsDeletedFalse(Long id);

    Optional<Document> findByIdAndOrganizationIdAndIsDeletedFalse(Long id, Long organizationId);

    Page<Document> findByOrganizationIdAndIsDeletedFalse(Long organizationId, Pageable pageable);

    List<Document> findByOrganizationIdAndIsDeletedFalse(Long organizationId);

    Page<Document> findByOrganizationIdAndFileTypeAndIsDeletedFalse(Long organizationId, String fileType, Pageable pageable);

    Page<Document> findByIsDeletedFalse(Pageable pageable);
}
