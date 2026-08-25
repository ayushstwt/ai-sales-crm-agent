package com.ayshriv.salescrm.document.service;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.document.dto.DocumentRetrievalResult;
import com.ayshriv.salescrm.document.dto.DocumentSearchRequest;
import com.ayshriv.salescrm.document.dto.DocumentUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {

    DocumentUploadResponse extractOnly(MultipartFile file);

    DocumentUploadResponse uploadDocument(MultipartFile file, String title);

    DocumentRetrievalResult retrieveSimilarChunks(String query, int topK);

    DocumentRetrievalResult retrieveSimilarChunksForOrg(Long organizationId, String query, int topK);

    ApiStatus getDocuments(DocumentSearchRequest request);

    ApiStatus getDocument(Long id);

    ApiStatus getDocumentChunks(Long documentId);

    ApiStatus deleteDocument(Long id);
}
