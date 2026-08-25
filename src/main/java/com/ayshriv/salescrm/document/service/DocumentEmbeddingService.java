package com.ayshriv.salescrm.document.service;

import java.util.List;

public interface DocumentEmbeddingService {

    List<Double> generateEmbedding(String text);

    List<List<Double>> generateEmbeddings(List<String> texts);

    double computeCosineSimilarity(List<Double> vectorA, List<Double> vectorB);
}
