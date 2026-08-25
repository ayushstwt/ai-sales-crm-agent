package com.ayshriv.salescrm.document.service.impl;

import com.ayshriv.salescrm.audit.entity.AuditSource;
import com.ayshriv.salescrm.audit.service.AuditLogService;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.resources.LogConstants;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.common.security.TenantContext;
import com.ayshriv.salescrm.common.security.TenantContextService;
import com.ayshriv.salescrm.document.dto.DocumentChunkDto;
import com.ayshriv.salescrm.document.dto.DocumentRetrievalResult;
import com.ayshriv.salescrm.document.dto.DocumentSearchRequest;
import com.ayshriv.salescrm.document.dto.DocumentUploadResponse;
import com.ayshriv.salescrm.document.entity.Document;
import com.ayshriv.salescrm.document.entity.DocumentChunk;
import com.ayshriv.salescrm.document.repository.DocumentChunkRepository;
import com.ayshriv.salescrm.document.repository.DocumentRepository;
import com.ayshriv.salescrm.document.service.DocumentChunker;
import com.ayshriv.salescrm.document.service.DocumentEmbeddingService;
import com.ayshriv.salescrm.document.service.DocumentService;
import com.ayshriv.salescrm.document.service.DocumentTextExtractor;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.repository.UserRepository;
import com.ayshriv.salescrm.user.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentServiceImpl.class);

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentTextExtractor textExtractor;
    private final DocumentChunker chunker;
    private final DocumentEmbeddingService embeddingService;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final TenantContextService tenantContextService;
    private final AuditLogService auditLogService;
    private final LogService logService;

    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository,
            DocumentTextExtractor textExtractor,
            DocumentChunker chunker,
            DocumentEmbeddingService embeddingService,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            TenantContextService tenantContextService,
            AuditLogService auditLogService,
            LogService logService
    ) {
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.textExtractor = textExtractor;
        this.chunker = chunker;
        this.embeddingService = embeddingService;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.tenantContextService = tenantContextService;
        this.auditLogService = auditLogService;
        this.logService = logService;
    }

    @Override
    public DocumentUploadResponse extractOnly(MultipartFile file) {
        LOGGER.info("DocumentService >> extractOnly called for file: {}", file != null ? file.getOriginalFilename() : "null");
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required and cannot be empty.");
        }

        try {
            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();
            String extractedText = textExtractor.extractText(file.getInputStream(), filename, contentType);
            String ext = getFileExtension(filename).toUpperCase();

            DocumentUploadResponse response = new DocumentUploadResponse();
            response.setFilename(filename);
            response.setFileType(ext);
            response.setFileSize(file.getSize());
            response.setExtractedText(extractedText);
            response.setMessage("Text extracted successfully.");
            return response;
        } catch (Exception e) {
            LOGGER.error("DocumentService >> Failed to extract text from file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract text from file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public DocumentUploadResponse uploadDocument(MultipartFile file, String title) {
        LOGGER.info("DocumentService >> uploadDocument called for file: {}, title: {}",
                file != null ? file.getOriginalFilename() : "null", title);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required and cannot be empty.");
        }

        TenantContext context = tenantContextService.getCurrentContext();
        Organization organization = resolveOrganization(context);
        User user = resolveUser(context);

        try {
            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();
            String ext = getFileExtension(filename).toUpperCase();
            String docTitle = (title != null && !title.isBlank()) ? title.trim() : filename;

            // 1. Extract text from uploaded document
            String extractedText = textExtractor.extractText(file.getInputStream(), filename, contentType);
            if (extractedText.isBlank()) {
                throw new IllegalArgumentException("Uploaded document contained no extractable text.");
            }

            // 2. Save Document entity
            Document document = new Document(
                    organization,
                    user,
                    filename,
                    ext,
                    file.getSize(),
                    docTitle
            );
            document = documentRepository.save(document);
            LOGGER.info("DocumentService >> Saved document entity ID: {} for organization: {}", document.getId(), organization.getId());

            // 3. Chunk text (~500 tokens with overlap)
            List<DocumentChunk> chunks = chunker.chunkText(extractedText, organization, document);

            // 4. Generate embeddings and save each chunk with organization_id
            List<DocumentChunkDto> chunkDtos = new ArrayList<>();
            for (DocumentChunk chunk : chunks) {
                // Generate embedding vector
                List<Double> vector = embeddingService.generateEmbedding(chunk.getContent());
                chunk.setEmbeddingVector(vector);
                chunk = documentChunkRepository.save(chunk);

                chunkDtos.add(new DocumentChunkDto(
                        chunk.getId(),
                        document.getId(),
                        document.getTitle(),
                        document.getFilename(),
                        chunk.getChunkIndex(),
                        chunk.getContent(),
                        chunk.getTokenCount(),
                        null
                ));
            }

            LOGGER.info("DocumentService >> Stored {} chunks with embeddings for document ID: {}", chunks.size(), document.getId());

            // 5. Audit log and user log
            auditLogService.logAction(
                    organization,
                    user,
                    LogConstants.DOCUMENT,
                    document.getId(),
                    LogConstants.UPLOAD,
                    AuditSource.MANUAL,
                    "Uploaded document '" + filename + "' (" + chunks.size() + " chunks indexed)"
            );

            if (user != null) {
                logService.createLog(user, LogConstants.DOCUMENT, LogConstants.UPLOAD, LocalDateTime.now(), null);
            }

            DocumentUploadResponse response = new DocumentUploadResponse();
            response.setDocumentId(document.getId());
            response.setFilename(document.getFilename());
            response.setFileType(document.getFileType());
            response.setFileSize(document.getFileSize());
            response.setTitle(document.getTitle());
            response.setChunkCount(chunks.size());
            response.setExtractedText(extractedText);
            response.setChunks(chunkDtos);
            response.setMessage("Document uploaded, chunked, and embedded successfully.");

            return response;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("DocumentService >> Error processing document upload: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload and index document: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentRetrievalResult retrieveSimilarChunks(String query, int topK) {
        LOGGER.info("DocumentService >> retrieveSimilarChunks called with query: '{}', topK: {}", query, topK);
        TenantContext context = tenantContextService.getCurrentContext();
        Organization organization = resolveOrganization(context);
        return retrieveSimilarChunksForOrg(organization.getId(), query, topK);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentRetrievalResult retrieveSimilarChunksForOrg(Long organizationId, String query, int topK) {
        LOGGER.info("DocumentService >> retrieveSimilarChunksForOrg called for orgId: {}, query: '{}', topK: {}",
                organizationId, query, topK);

        if (query == null || query.isBlank()) {
            return new DocumentRetrievalResult(query, organizationId, 0, Collections.emptyList());
        }

        int k = topK > 0 ? topK : 5;

        // RULE #5 ENFORCEMENT: Filter by organization_id FIRST in the database query
        List<DocumentChunk> orgChunks = documentChunkRepository.findByOrganizationIdAndIsDeletedFalse(organizationId);
        LOGGER.info("DocumentService >> Filtered {} total chunks for orgId: {} before similarity ranking",
                orgChunks.size(), organizationId);

        if (orgChunks.isEmpty()) {
            return new DocumentRetrievalResult(query, organizationId, 0, Collections.emptyList());
        }

        // Generate query embedding
        List<Double> queryVector = embeddingService.generateEmbedding(query);

        // Compute similarity against only the current tenant's chunks
        List<DocumentChunkDto> scoredMatches = new ArrayList<>();
        for (DocumentChunk chunk : orgChunks) {
            List<Double> chunkVector = chunk.getEmbeddingVector();
            double similarity = embeddingService.computeCosineSimilarity(queryVector, chunkVector);

            Document doc = chunk.getDocument();
            String docTitle = doc != null ? doc.getTitle() : "Document #" + chunk.getId();
            String docFilename = doc != null ? doc.getFilename() : "document.txt";

            DocumentChunkDto dto = new DocumentChunkDto(
                    chunk.getId(),
                    doc != null ? doc.getId() : null,
                    docTitle,
                    docFilename,
                    chunk.getChunkIndex(),
                    chunk.getContent(),
                    chunk.getTokenCount(),
                    similarity
            );
            scoredMatches.add(dto);
        }

        // Rank by descending cosine similarity
        scoredMatches.sort(Comparator.comparing(DocumentChunkDto::getSimilarityScore, Comparator.nullsLast(Comparator.reverseOrder())));

        List<DocumentChunkDto> topMatches = scoredMatches.stream()
                .limit(k)
                .collect(Collectors.toList());

        return new DocumentRetrievalResult(query, organizationId, orgChunks.size(), topMatches);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus getDocuments(DocumentSearchRequest request) {
        LOGGER.info("DocumentService >> getDocuments called");
        try {
            TenantContext context = tenantContextService.getCurrentContext();
            Organization organization = resolveOrganization(context);

            int pageNumber = (request != null && request.getPageNumber() != null && request.getPageNumber() > 0)
                    ? request.getPageNumber() - 1 : 0;
            int pageSize = (request != null && request.getPageSize() != null && request.getPageSize() > 0)
                    ? request.getPageSize() : 10;
            String orderBy = (request != null && request.getOrderBy() != null && !request.getOrderBy().isBlank())
                    ? request.getOrderBy() : "id";
            Sort.Direction direction = (request != null && "ASC".equalsIgnoreCase(request.getOrderDirection()))
                    ? Sort.Direction.ASC : Sort.Direction.DESC;

            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, orderBy));
            Page<Document> page;

            if (request != null && request.getFileType() != null && !request.getFileType().isBlank()) {
                page = documentRepository.findByOrganizationIdAndFileTypeAndIsDeletedFalse(organization.getId(), request.getFileType().toUpperCase(), pageable);
            } else {
                page = documentRepository.findByOrganizationIdAndIsDeletedFalse(organization.getId(), pageable);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.LIST_SUCCESS, LogConstants.DOCUMENT);
            status.setTotal(page.getTotalElements());
            status.setDocuments(page.getContent());
            return status;

        } catch (Exception e) {
            LOGGER.error("DocumentService >> getDocuments error: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), LogConstants.DOCUMENT);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus getDocument(Long id) {
        LOGGER.info("DocumentService >> getDocument called for id: {}", id);
        try {
            TenantContext context = tenantContextService.getCurrentContext();
            Organization organization = resolveOrganization(context);

            Optional<Document> docOpt = documentRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, organization.getId());
            if (docOpt.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DETAIL_FAILURE, LogConstants.DOCUMENT);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.DETAIL_SUCCESS, LogConstants.DOCUMENT);
            status.setDocument(docOpt.get());
            return status;

        } catch (Exception e) {
            LOGGER.error("DocumentService >> getDocument error: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), LogConstants.DOCUMENT);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus getDocumentChunks(Long documentId) {
        LOGGER.info("DocumentService >> getDocumentChunks called for documentId: {}", documentId);
        try {
            TenantContext context = tenantContextService.getCurrentContext();
            Organization organization = resolveOrganization(context);

            Optional<Document> docOpt = documentRepository.findByIdAndOrganizationIdAndIsDeletedFalse(documentId, organization.getId());
            if (docOpt.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DETAIL_FAILURE, LogConstants.DOCUMENT);
            }

            List<DocumentChunk> chunks = documentChunkRepository.findByOrganizationIdAndDocumentIdAndIsDeletedFalseOrderByChunkIndexAsc(
                    organization.getId(), documentId
            );

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.LIST_SUCCESS, LogConstants.DOCUMENT_CHUNK);
            status.setDocument(docOpt.get());
            status.setTotal((long) chunks.size());
            return status;

        } catch (Exception e) {
            LOGGER.error("DocumentService >> getDocumentChunks error: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), LogConstants.DOCUMENT_CHUNK);
        }
    }

    @Override
    @Transactional
    public ApiStatus deleteDocument(Long id) {
        LOGGER.info("DocumentService >> deleteDocument called for id: {}", id);
        try {
            TenantContext context = tenantContextService.getCurrentContext();
            Organization organization = resolveOrganization(context);
            User user = resolveUser(context);

            Optional<Document> docOpt = documentRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, organization.getId());
            if (docOpt.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DELETE_FAILURE, LogConstants.DOCUMENT);
            }

            Document document = docOpt.get();
            document.setIsDeleted(true);
            document.setUpdatedOn(LocalDateTime.now());
            documentRepository.save(document);

            // Soft-delete linked chunks
            List<DocumentChunk> chunks = documentChunkRepository.findByOrganizationIdAndDocumentIdAndIsDeletedFalseOrderByChunkIndexAsc(
                    organization.getId(), id
            );
            for (DocumentChunk chunk : chunks) {
                chunk.setIsDeleted(true);
                chunk.setUpdatedOn(LocalDateTime.now());
                documentChunkRepository.save(chunk);
            }

            auditLogService.logAction(
                    organization,
                    user,
                    LogConstants.DOCUMENT,
                    document.getId(),
                    LogConstants.DELETE,
                    AuditSource.MANUAL,
                    "Deleted document '" + document.getFilename() + "'"
            );

            if (user != null) {
                logService.createLog(user, LogConstants.DOCUMENT, LogConstants.DELETE, LocalDateTime.now(), null);
            }

            return Resources.setStatus(Constants.SUCCESS, Constants.DELETE_SUCCESS, LogConstants.DOCUMENT);

        } catch (Exception e) {
            LOGGER.error("DocumentService >> deleteDocument error: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), LogConstants.DOCUMENT);
        }
    }

    private Organization resolveOrganization(TenantContext context) {
        if (context != null && context.getOrganizationId() != null) {
            return organizationRepository.findByIdAndIsDeletedFalse(context.getOrganizationId())
                    .orElseThrow(() -> new IllegalStateException("Organization not found for id: " + context.getOrganizationId()));
        }
        return organizationRepository.findAll().stream()
                .filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active organization found"));
    }

    private User resolveUser(TenantContext context) {
        if (context != null && context.getUserId() != null) {
            return userRepository.findByIdAndIsDeletedFalse(context.getUserId()).orElse(null);
        }
        return userRepository.findAll().stream()
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .findFirst()
                .orElse(null);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "TXT";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
