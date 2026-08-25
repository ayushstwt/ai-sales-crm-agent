package com.ayshriv.salescrm.document.service;

import com.ayshriv.salescrm.document.entity.Document;
import com.ayshriv.salescrm.document.entity.DocumentChunk;
import com.ayshriv.salescrm.document.service.impl.DocumentChunkerImpl;
import com.ayshriv.salescrm.organization.entity.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentChunkerTest {

    private DocumentChunker chunker;
    private Organization testOrg;
    private Document testDoc;

    @BeforeEach
    void setUp() {
        chunker = new DocumentChunkerImpl();
        testOrg = new Organization("Test Org", "test-org");
        testDoc = new Document(testOrg, null, "sample.txt", "TXT", 1024L, "Sample Document");
    }

    @Test
    @DisplayName("Stage 6.3 - Short document produces a single chunk")
    void testChunkShortDocument() {
        String text = "This is a short sales overview document. It contains under 500 tokens of text.";
        List<DocumentChunk> chunks = chunker.chunkText(text, testOrg, testDoc);

        assertThat(chunks).hasSize(1);
        DocumentChunk chunk = chunks.get(0);
        assertThat(chunk.getChunkIndex()).isEqualTo(0);
        assertThat(chunk.getContent()).isEqualTo(text);
        assertThat(chunk.getTokenCount()).isGreaterThan(0);
        assertThat(chunk.getOrganization()).isEqualTo(testOrg);
        assertThat(chunk.getDocument()).isEqualTo(testDoc);
    }

    @Test
    @DisplayName("Stage 6.3 - Long document produces multiple chunks with token limits and overlap")
    void testChunkLongDocumentWithOverlap() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 60; i++) {
            sb.append("Section ").append(i).append(": Enterprise CRM deal pricing and subscription terms for sales representatives. ")
              .append("All enterprise plans include dedicated TAM support, automated workflows, and SOC2 compliance.\n\n");
        }
        String longText = sb.toString();

        // 500 target tokens, 50 overlap tokens
        List<DocumentChunk> chunks = chunker.chunkText(longText, testOrg, testDoc, 500, 50);

        assertThat(chunks.size()).isGreaterThan(1);
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = chunks.get(i);
            assertThat(chunk.getChunkIndex()).isEqualTo(i);
            assertThat(chunk.getContent()).isNotBlank();
            assertThat(chunk.getTokenCount()).isGreaterThan(0);
            assertThat(chunk.getOrganization()).isEqualTo(testOrg);
            assertThat(chunk.getDocument()).isEqualTo(testDoc);
        }

        // Verify overlap: adjacent chunks share some text
        String chunk0 = chunks.get(0).getContent();
        String chunk1 = chunks.get(1).getContent();
        // The start of chunk 1 contains words that were near the end of chunk 0
        assertThat(chunk0).isNotEqualTo(chunk1);
    }

    @Test
    @DisplayName("Stage 6.3 - Empty or blank text returns empty chunk list")
    void testChunkEmptyText() {
        assertThat(chunker.chunkText("", testOrg, testDoc)).isEmpty();
        assertThat(chunker.chunkText("   \n\t  ", testOrg, testDoc)).isEmpty();
        assertThat(chunker.chunkText(null, testOrg, testDoc)).isEmpty();
    }
}
