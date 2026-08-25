package com.ayshriv.salescrm.document.service;

import com.ayshriv.salescrm.document.service.impl.DocumentTextExtractorImpl;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentTextExtractorTest {

    private DocumentTextExtractor textExtractor;

    @BeforeEach
    void setUp() {
        textExtractor = new DocumentTextExtractorImpl();
    }

    @Test
    @DisplayName("Stage 6.2 - Confirm TXT extraction extracts correct text")
    void testExtractTextFromTxt() throws IOException {
        String originalContent = "SalesPilot CRM Document\nThis is a plain text proposal for Enterprise Client Acme Corp.\nKey terms: $50,000 annual license.";
        byte[] bytes = originalContent.getBytes(StandardCharsets.UTF_8);

        String extracted = textExtractor.extractText(bytes, "proposal.txt", "text/plain");

        assertThat(extracted).isNotBlank();
        assertThat(extracted).contains("SalesPilot CRM Document");
        assertThat(extracted).contains("Enterprise Client Acme Corp");
        assertThat(extracted).contains("$50,000 annual license");
    }

    @Test
    @DisplayName("Stage 6.2 - Confirm PDF extraction extracts correct text using PDFBox")
    void testExtractTextFromPdf() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("SalesPilot Master Services Agreement");
                contentStream.newLineAtOffset(0, -20);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.showText("Clause 1: 99.9% uptime SLA with 24/7 priority enterprise support.");
                contentStream.endText();
            }
            document.save(baos);
        }

        byte[] pdfBytes = baos.toByteArray();
        String extracted = textExtractor.extractText(pdfBytes, "contract.pdf", "application/pdf");

        assertThat(extracted).isNotBlank();
        assertThat(extracted).contains("SalesPilot Master Services Agreement");
        assertThat(extracted).contains("Clause 1: 99.9% uptime SLA");
    }

    @Test
    @DisplayName("Stage 6.2 - Confirm DOCX extraction extracts correct text using Apache POI")
    void testExtractTextFromDocx() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XWPFDocument docx = new XWPFDocument()) {
            XWPFParagraph titleParagraph = docx.createParagraph();
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setBold(true);
            titleRun.setText("Sales Strategy & Product Roadmap 2026");

            XWPFParagraph bodyParagraph = docx.createParagraph();
            XWPFRun bodyRun = bodyParagraph.createRun();
            bodyRun.setText("Targeting Fortune 500 SaaS companies with autonomous AI agent workflows.");

            docx.write(baos);
        }

        byte[] docxBytes = baos.toByteArray();
        String extracted = textExtractor.extractText(docxBytes, "strategy.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        assertThat(extracted).isNotBlank();
        assertThat(extracted).contains("Sales Strategy & Product Roadmap 2026");
        assertThat(extracted).contains("Targeting Fortune 500 SaaS companies");
    }
}
