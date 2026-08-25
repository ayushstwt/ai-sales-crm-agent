package com.ayshriv.salescrm.document.service.impl;

import com.ayshriv.salescrm.document.service.DocumentTextExtractor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class DocumentTextExtractorImpl implements DocumentTextExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentTextExtractorImpl.class);

    @Override
    public String extractText(InputStream inputStream, String filename, String contentType) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        return extractText(bytes, filename, contentType);
    }

    @Override
    public String extractText(byte[] bytes, String filename, String contentType) throws IOException {
        String ext = getFileExtension(filename).toLowerCase();
        LOGGER.info("DocumentTextExtractor >> Extracting text for file: {} with extension: {}", filename, ext);

        if ("pdf".equals(ext) || (contentType != null && contentType.toLowerCase().contains("pdf"))) {
            return extractFromPdf(bytes);
        } else if ("docx".equals(ext) || (contentType != null && (contentType.contains("wordprocessingml") || contentType.contains("officedocument")))) {
            return extractFromDocx(bytes);
        } else if ("txt".equals(ext) || "text".equals(ext) || "md".equals(ext) || (contentType != null && contentType.toLowerCase().startsWith("text/"))) {
            return extractFromTxt(bytes);
        } else {
            // Default fallback if plain text can be read, else reject
            try {
                return extractFromTxt(bytes);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unsupported file type for file: " + filename + ". Only PDF, DOCX, and TXT are supported.");
            }
        }
    }

    private String extractFromPdf(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return text != null ? text.trim() : "";
        } catch (Exception e) {
            LOGGER.error("Failed to extract text from PDF: {}", e.getMessage(), e);
            throw new IOException("Failed to extract text from PDF: " + e.getMessage(), e);
        }
    }

    private String extractFromDocx(byte[] bytes) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             XWPFDocument docx = new XWPFDocument(bais);
             XWPFWordExtractor extractor = new XWPFWordExtractor(docx)) {
            String text = extractor.getText();
            return text != null ? text.trim() : "";
        } catch (Exception e) {
            LOGGER.error("Failed to extract text from DOCX: {}", e.getMessage(), e);
            throw new IOException("Failed to extract text from DOCX: " + e.getMessage(), e);
        }
    }

    private String extractFromTxt(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8).trim();
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
