package com.ourexists.omes.ai.service;

import com.ourexists.omes.ai.config.AiInspectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class InspectionKnowledgeService {

    private final AiInspectionProperties properties;
    private final VectorStore vectorStore;
    private final Map<String, String> fallbackKnowledge = new ConcurrentHashMap<>();

    public InspectionKnowledgeService(AiInspectionProperties properties,
                                      ObjectProvider<VectorStore> vectorStoreProvider,
                                      ObjectProvider<EmbeddingModel> embeddingModelProvider) {
        this.properties = properties;
        VectorStore configuredStore = vectorStoreProvider.getIfAvailable();
        if (configuredStore != null) {
            this.vectorStore = configuredStore;
        } else {
            EmbeddingModel embeddingModel = embeddingModelProvider.getIfAvailable();
            this.vectorStore = embeddingModel != null ? SimpleVectorStore.builder(embeddingModel).build() : null;
        }
    }

    public void replaceKnowledge(List<Document> documents) {
        fallbackKnowledge.clear();
        if (documents == null || documents.isEmpty()) {
            return;
        }
        if (vectorStore != null) {
            vectorStore.add(documents);
            return;
        }
        for (Document document : documents) {
            fallbackKnowledge.put(document.getId(), document.getText());
        }
    }

    public int addKnowledge(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return 0;
        }
        if (vectorStore != null) {
            vectorStore.add(documents);
            return documents.size();
        }
        for (Document document : documents) {
            String id = document.getId();
            if (id == null || id.isBlank()) {
                id = UUID.randomUUID().toString();
            }
            fallbackKnowledge.put(id, document.getText());
        }
        return documents.size();
    }

    public List<String> searchKnowledge(String question, int topK) {
        if (question == null || question.isBlank()) {
            return Collections.emptyList();
        }
        int actualTopK = Math.max(1, topK);
        if (vectorStore != null) {
            SearchRequest request = SearchRequest.builder()
                    .query(question)
                    .topK(actualTopK)
                    .build();
            List<Document> matches = vectorStore.similaritySearch(request);
            if (matches == null || matches.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> docs = new ArrayList<>();
            for (Document item : matches) {
                docs.add(item.getText());
            }
            return docs;
        }

        List<String> docs = new ArrayList<>(fallbackKnowledge.values());
        if (docs.isEmpty()) {
            return docs;
        }
        if (docs.size() > actualTopK) {
            return docs.subList(0, actualTopK);
        }
        return docs;
    }

    public String getProviderSummary() {
        if (vectorStore != null) {
            return properties.getVectorProvider() + " (spring-ai-vector-store)";
        }
        return "in-memory-fallback";
    }
}
