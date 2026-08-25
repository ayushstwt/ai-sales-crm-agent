package com.ayshriv.salescrm.ai.tool;

import com.ayshriv.salescrm.ai.service.ToolExecutionService;
import com.ayshriv.salescrm.ai.tool.dto.KnowledgeChunkSummary;
import com.ayshriv.salescrm.ai.tool.dto.RetrieveKnowledgeBaseInput;
import com.ayshriv.salescrm.ai.tool.dto.RetrieveKnowledgeBaseOutput;
import com.ayshriv.salescrm.document.dto.DocumentChunkDto;
import com.ayshriv.salescrm.document.dto.DocumentRetrievalResult;
import com.ayshriv.salescrm.document.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * AI Tool for RAG document retrieval.
 * Architecture Rule #2: AI -> Tool -> DocumentService -> TenantContext -> Repository
 * Architecture Rule #5: Tenant filtering enforced before similarity ranking.
 */
@Component
public class DocumentTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentTools.class);

    private final DocumentService documentService;
    private final ToolExecutionService toolExecutionService;
    private final ObjectMapper objectMapper;

    public DocumentTools(
            DocumentService documentService,
            ToolExecutionService toolExecutionService,
            ObjectMapper objectMapper
    ) {
        this.documentService = documentService;
        this.toolExecutionService = toolExecutionService;
        this.objectMapper = objectMapper;
    }

    public RetrieveKnowledgeBaseOutput retrieveKnowledgeBase(RetrieveKnowledgeBaseInput input) {
        LOGGER.info("DocumentTools >> retrieveKnowledgeBase called with query: '{}', topK: {}",
                input != null ? input.getQuery() : null, input != null ? input.getTopK() : null);

        long startTime = System.currentTimeMillis();
        String inputJson = serialize(input);

        try {
            if (input == null || input.getQuery() == null || input.getQuery().isBlank()) {
                RetrieveKnowledgeBaseOutput output = new RetrieveKnowledgeBaseOutput(false, "Search query is required.");
                long duration = System.currentTimeMillis() - startTime;
                toolExecutionService.recordExecution("retrieveKnowledgeBase", inputJson, serialize(output), "SUCCESS", duration);
                return output;
            }

            int topK = input.getTopK() != null && input.getTopK() > 0 ? input.getTopK() : 4;
            DocumentRetrievalResult retrievalResult = documentService.retrieveSimilarChunks(input.getQuery(), topK);

            List<KnowledgeChunkSummary> chunkSummaries = new ArrayList<>();
            if (retrievalResult != null && retrievalResult.getMatches() != null) {
                for (DocumentChunkDto match : retrievalResult.getMatches()) {
                    chunkSummaries.add(new KnowledgeChunkSummary(
                            match.getId(),
                            match.getDocumentId(),
                            match.getDocumentTitle(),
                            match.getDocumentFilename(),
                            match.getChunkIndex(),
                            match.getContent(),
                            match.getSimilarityScore()
                    ));
                }
            }

            RetrieveKnowledgeBaseOutput output = new RetrieveKnowledgeBaseOutput();
            output.setSuccess(true);
            output.setQuery(input.getQuery());
            output.setTotalMatches(chunkSummaries.size());
            output.setChunks(chunkSummaries);
            output.setMessage("Retrieved " + chunkSummaries.size() + " relevant document snippet(s). Always cite document titles in response.");

            long duration = System.currentTimeMillis() - startTime;
            toolExecutionService.recordExecution("retrieveKnowledgeBase", inputJson, serialize(output), "SUCCESS", duration);
            return output;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.error("DocumentTools >> retrieveKnowledgeBase error: {}", e.getMessage(), e);
            toolExecutionService.recordExecution("retrieveKnowledgeBase", inputJson, "Error: " + e.getMessage(), "ERROR", duration);
            return new RetrieveKnowledgeBaseOutput(false, "Error retrieving knowledge base documents: " + e.getMessage());
        }
    }

    public FunctionCallback retrieveKnowledgeBaseFunctionCallback() {
        return FunctionCallbackWrapper.builder((Function<RetrieveKnowledgeBaseInput, RetrieveKnowledgeBaseOutput>) this::retrieveKnowledgeBase)
                .withName("retrieveKnowledgeBase")
                .withDescription("Search and retrieve relevant text snippets from the organization's uploaded knowledge base, contracts, proposal templates, product docs, and guidelines. ALWAYS cite which document title or filename you referenced in your final answer.")
                .withInputType(RetrieveKnowledgeBaseInput.class)
                .build();
    }

    private String serialize(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
