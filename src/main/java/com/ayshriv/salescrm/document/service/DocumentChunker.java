package com.ayshriv.salescrm.document.service;

import com.ayshriv.salescrm.document.entity.Document;
import com.ayshriv.salescrm.document.entity.DocumentChunk;
import com.ayshriv.salescrm.organization.entity.Organization;

import java.util.List;

public interface DocumentChunker {

    int DEFAULT_CHUNK_SIZE_TOKENS = 500;
    int DEFAULT_OVERLAP_TOKENS = 50;

    List<DocumentChunk> chunkText(String text, Organization organization, Document document);

    List<DocumentChunk> chunkText(String text, Organization organization, Document document, int targetTokens, int overlapTokens);

    int estimateTokenCount(String text);
}
