package com.ayshriv.salescrm.document.service.impl;

import com.ayshriv.salescrm.document.service.DocumentEmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DocumentEmbeddingServiceImpl implements DocumentEmbeddingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentEmbeddingServiceImpl.class);
    public static final int EMBEDDING_DIMENSION = 1536;

    private final EmbeddingModel embeddingModel;

    @Autowired
    public DocumentEmbeddingServiceImpl(@Autowired(required = false) EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public List<Double> generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        if (embeddingModel != null) {
            try {
                LOGGER.info("DocumentEmbeddingService >> Calling Spring AI EmbeddingModel");
                List<Double> embedding = embeddingModel.embed(text);
                if (embedding != null && !embedding.isEmpty()) {
                    return embedding;
                }
            } catch (Exception e) {
                LOGGER.warn("DocumentEmbeddingService >> Real embedding call failed ({}). Falling back to deterministic local embedding.", e.getMessage());
            }
        }

        return generateDeterministicVector(text);
    }

    @Override
    public List<List<Double>> generateEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<Double>> results = new ArrayList<>(texts.size());
        for (String text : texts) {
            results.add(generateEmbedding(text));
        }
        return results;
    }

    @Override
    public double computeCosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA == null || vectorB == null || vectorA.isEmpty() || vectorB.isEmpty()) {
            return 0.0;
        }

        int size = Math.min(vectorA.size(), vectorB.size());
        if (size == 0) return 0.0;

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < size; i++) {
            double a = vectorA.get(i);
            double b = vectorB.get(i);
            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Deterministic vector generator for test/offline environments.
     * Uses character n-grams and hashing to produce consistent 1536-dimensional unit vectors
     * where texts with shared terms have high cosine similarity.
     */
    public static List<Double> generateDeterministicVector(String text) {
        double[] vector = new double[EMBEDDING_DIMENSION];
        String clean = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] tokens = clean.split("\\s+");

        for (String token : tokens) {
            if (token.isBlank()) continue;
            int hash = Math.abs(token.hashCode());
            int idx = hash % EMBEDDING_DIMENSION;
            vector[idx] += 1.0;

            if (token.length() >= 3) {
                for (int i = 0; i <= token.length() - 3; i++) {
                    String sub = token.substring(i, i + 3);
                    int subHash = Math.abs(sub.hashCode());
                    vector[subHash % EMBEDDING_DIMENSION] += 0.3;
                }
            }
        }

        double sumSquares = 0.0;
        for (double v : vector) {
            sumSquares += v * v;
        }

        List<Double> result = new ArrayList<>(EMBEDDING_DIMENSION);
        if (sumSquares > 0.0) {
            double norm = Math.sqrt(sumSquares);
            for (double v : vector) {
                result.add(v / norm);
            }
        } else {
            for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
                result.add(0.0);
            }
        }
        return result;
    }
}
