package com.ayshriv.salescrm.document.repository;

import com.ayshriv.salescrm.document.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    Optional<DocumentChunk> findByIdAndIsDeletedFalse(Long id);

    Optional<DocumentChunk> findByIdAndOrganizationIdAndIsDeletedFalse(Long id, Long organizationId);

    List<DocumentChunk> findByOrganizationIdAndIsDeletedFalse(Long organizationId);

    List<DocumentChunk> findByOrganizationIdAndDocumentIdAndIsDeletedFalseOrderByChunkIndexAsc(Long organizationId, Long documentId);

    List<DocumentChunk> findByDocumentIdAndIsDeletedFalseOrderByChunkIndexAsc(Long documentId);

    long countByOrganizationIdAndIsDeletedFalse(Long organizationId);
}
