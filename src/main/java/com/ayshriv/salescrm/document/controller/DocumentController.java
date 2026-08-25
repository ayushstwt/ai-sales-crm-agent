package com.ayshriv.salescrm.document.controller;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.document.dto.DocumentRetrievalResult;
import com.ayshriv.salescrm.document.dto.DocumentSearchRequest;
import com.ayshriv.salescrm.document.dto.DocumentUploadResponse;
import com.ayshriv.salescrm.document.service.DocumentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentUploadResponse> extractTextOnly(@RequestPart("file") MultipartFile file) {
        DocumentUploadResponse response = documentService.extractOnly(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title
    ) {
        DocumentUploadResponse response = documentService.uploadDocument(file, title);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/retrieve")
    public ResponseEntity<DocumentRetrievalResult> retrieveChunks(
            @RequestParam("query") String query,
            @RequestParam(value = "topK", defaultValue = "5") int topK
    ) {
        DocumentRetrievalResult result = documentService.retrieveSimilarChunks(query, topK);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<MappingJacksonValue> listDocuments(@ModelAttribute DocumentSearchRequest request) {
        ApiStatus status = documentService.getDocuments(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "documents", "total"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> viewDocument(@PathVariable Long id) {
        ApiStatus status = documentService.getDocument(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "document"));
    }

    @GetMapping("/{id}/chunks")
    public ResponseEntity<MappingJacksonValue> viewDocumentChunks(@PathVariable Long id) {
        ApiStatus status = documentService.getDocumentChunks(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "document", "total"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> deleteDocument(@PathVariable Long id) {
        ApiStatus status = documentService.deleteDocument(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text"));
    }
}
