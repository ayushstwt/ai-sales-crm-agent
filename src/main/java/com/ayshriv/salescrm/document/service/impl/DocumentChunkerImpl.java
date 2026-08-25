package com.ayshriv.salescrm.document.service.impl;

import com.ayshriv.salescrm.document.entity.Document;
import com.ayshriv.salescrm.document.entity.DocumentChunk;
import com.ayshriv.salescrm.document.service.DocumentChunker;
import com.ayshriv.salescrm.organization.entity.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DocumentChunkerImpl implements DocumentChunker {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentChunkerImpl.class);

    // 1 token ~= 4 characters / 0.75 words
    private static final int CHARS_PER_TOKEN = 4;

    @Override
    public List<DocumentChunk> chunkText(String text, Organization organization, Document document) {
        return chunkText(text, organization, document, DEFAULT_CHUNK_SIZE_TOKENS, DEFAULT_OVERLAP_TOKENS);
    }

    @Override
    public List<DocumentChunk> chunkText(String text, Organization organization, Document document, int targetTokens, int overlapTokens) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String cleanedText = text.replace("\r\n", "\n").trim();
        int targetChars = Math.max(100, targetTokens * CHARS_PER_TOKEN);
        int overlapChars = Math.max(0, overlapTokens * CHARS_PER_TOKEN);

        LOGGER.info("DocumentChunker >> Chunking text of length {} chars into ~{} tokens (~{} chars) chunks with {} overlap",
                cleanedText.length(), targetTokens, targetChars, overlapChars);

        List<DocumentChunk> chunks = new ArrayList<>();

        if (cleanedText.length() <= targetChars) {
            int tokenCount = estimateTokenCount(cleanedText);
            chunks.add(new DocumentChunk(organization, document, 0, cleanedText, tokenCount));
            return chunks;
        }

        int start = 0;
        int chunkIndex = 0;

        while (start < cleanedText.length()) {
            int end = Math.min(start + targetChars, cleanedText.length());

            if (end < cleanedText.length()) {
                // Find nearest good boundary before end (e.g. newline, period, space)
                int boundary = findNearestBoundary(cleanedText, start, end);
                if (boundary > start + (targetChars / 2)) {
                    end = boundary;
                }
            }

            String chunkContent = cleanedText.substring(start, end).trim();
            if (!chunkContent.isEmpty()) {
                int tokenCount = estimateTokenCount(chunkContent);
                DocumentChunk chunk = new DocumentChunk(organization, document, chunkIndex++, chunkContent, tokenCount);
                chunks.add(chunk);
            }

            if (end >= cleanedText.length()) {
                break;
            }

            // Move start forward with overlap
            int nextStart = end - overlapChars;
            if (nextStart <= start) {
                nextStart = end; // Ensure forward progress
            } else {
                // Adjust nextStart to a space boundary if possible
                int spaceBoundary = cleanedText.indexOf(' ', nextStart);
                if (spaceBoundary != -1 && spaceBoundary < end) {
                    nextStart = spaceBoundary + 1;
                }
            }
            start = nextStart;
        }

        LOGGER.info("DocumentChunker >> Created {} chunks for document ID: {}", chunks.size(), document != null ? document.getId() : null);
        return chunks;
    }

    private int findNearestBoundary(String text, int start, int end) {
        // Look for double newlines (paragraph boundary)
        int lastDoubleNewline = text.lastIndexOf("\n\n", end);
        if (lastDoubleNewline > start + 100) {
            return lastDoubleNewline + 2;
        }

        // Look for single newline
        int lastNewline = text.lastIndexOf('\n', end);
        if (lastNewline > start + 100) {
            return lastNewline + 1;
        }

        // Look for sentence boundary (. ! ? followed by space or newline)
        for (int i = end - 1; i >= start + 100; i--) {
            char c = text.charAt(i);
            if ((c == '.' || c == '!' || c == '?') && i + 1 < text.length() && Character.isWhitespace(text.charAt(i + 1))) {
                return i + 1;
            }
        }

        // Look for word space boundary
        int lastSpace = text.lastIndexOf(' ', end);
        if (lastSpace > start + 50) {
            return lastSpace + 1;
        }

        return end;
    }

    @Override
    public int estimateTokenCount(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        // Approximate token count: words * 1.3 or length / 4
        String[] words = text.trim().split("\\s+");
        return (int) Math.ceil(words.length * 1.33);
    }
}
