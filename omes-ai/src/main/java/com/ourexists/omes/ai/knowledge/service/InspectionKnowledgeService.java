package com.ourexists.omes.ai.knowledge.service;

import com.ourexists.omes.ai.config.AiLlmProviderProperties;
import com.ourexists.omes.ai.config.AgentProperties;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class InspectionKnowledgeService {

    private final AgentProperties properties;
    private final OpenAiEmbeddingModel embeddingModel;
    private final RestTemplate restTemplate;
    private final List<String> localKnowledge = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, float[]> localVectors = new ConcurrentHashMap<>();
    private final Map<String, String> localTextById = new ConcurrentHashMap<>();
    private volatile String activeBatchId = UUID.randomUUID().toString();

    public InspectionKnowledgeService(AgentProperties properties,
                                      AiLlmProviderProperties providerProperties,
                                      RestTemplate restTemplate,
                                      @Value("${ai.llm.openai.embedding-model:text-embedding-3-small}") String embeddingModelName) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        AiLlmProviderProperties.ProviderConfig openai = providerProperties.getOpenai();
        if (openai == null || !StringUtils.hasText(openai.getApiKey())) {
            this.embeddingModel = null;
            return;
        }
        String baseUrl = StringUtils.hasText(openai.getBaseUrl()) ? openai.getBaseUrl().trim() : "https://api.openai.com";
        String model = StringUtils.hasText(openai.getEmbeddingModel())
                ? openai.getEmbeddingModel().trim()
                : (StringUtils.hasText(embeddingModelName) ? embeddingModelName.trim() : "text-embedding-3-small");
        this.embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(openai.getApiKey().trim())
                .baseUrl(baseUrl)
                .modelName(model)
                .build();
    }

    public void replaceKnowledge(List<String> docs) {
        localKnowledge.clear();
        localVectors.clear();
        localTextById.clear();
        activeBatchId = UUID.randomUUID().toString();
        if (docs == null || docs.isEmpty()) {
            return;
        }
        addKnowledge(docs);
    }

    public int addKnowledge(List<String> docs) {
        if (docs == null || docs.isEmpty()) {
            return 0;
        }
        for (String text : docs) {
            if (!StringUtils.hasText(text)) {
                continue;
            }
            String id = UUID.randomUUID().toString();
            localKnowledge.add(text);
            float[] vector = embedOrNull(text);
            localTextById.put(id, text);
            if (vector != null) {
                localVectors.put(id, vector);
            }
            if (isQdrantEnabled() && vector != null) {
                upsertQdrantPoint(id, text, vector);
            }
        }
        return docs.size();
    }

    public List<String> searchKnowledge(String question, int topK) {
        if (question == null || question.isBlank()) {
            return Collections.emptyList();
        }
        int actualTopK = Math.max(1, topK);
        if (localKnowledge.isEmpty()) {
            return Collections.emptyList();
        }
        float[] queryVector = embedOrNull(question);
        if (isQdrantEnabled() && queryVector != null) {
            List<String> qdrantMatches = searchFromQdrant(queryVector, actualTopK);
            if (!qdrantMatches.isEmpty()) {
                return qdrantMatches;
            }
        }
        if (queryVector == null) {
            return topLocalTexts(actualTopK);
        }
        List<ScoredText> scored = new ArrayList<>(localVectors.size());
        for (Map.Entry<String, float[]> entry : localVectors.entrySet()) {
            float[] vector = entry.getValue();
            if (vector == null || vector.length == 0) {
                continue;
            }
            scored.add(new ScoredText(entry.getKey(), cosineSimilarity(queryVector, vector)));
        }
        scored.sort((a, b) -> Double.compare(b.score, a.score));
        List<String> result = new ArrayList<>();
        for (ScoredText item : scored) {
            String text = findLocalTextById(item.id);
            if (text == null) {
                continue;
            }
            result.add(text);
            if (result.size() >= actualTopK) {
                break;
            }
        }
        if (result.isEmpty()) {
            return topLocalTexts(actualTopK);
        }
        return result;
    }

    public String getProviderSummary() {
        if (isQdrantEnabled() && embeddingModel != null) {
            return "qdrant (langchain4j-embedding + qdrant-http)";
        }
        if (embeddingModel != null) {
            return "in-memory (langchain4j-embedding)";
        }
        return "in-memory-fallback";
    }

    private float[] embedOrNull(String text) {
        if (embeddingModel == null || !StringUtils.hasText(text)) {
            return null;
        }
        try {
            Embedding embedding = embeddingModel.embed(text).content();
            return embedding == null ? null : embedding.vector();
        } catch (Exception ex) {
            log.warn("Embedding failed, fallback to plain retrieval: {}", ex.getMessage());
            return null;
        }
    }

    private boolean isQdrantEnabled() {
        return "qdrant".equalsIgnoreCase(properties.getVectorProvider());
    }

    private void upsertQdrantPoint(String id, String text, float[] vector) {
        AgentProperties.Qdrant cfg = properties.getQdrant();
        if (cfg == null || !StringUtils.hasText(cfg.getHost()) || !StringUtils.hasText(cfg.getCollectionName())) {
            return;
        }
        ensureQdrantCollection(cfg, vector.length);
        String url = qdrantBaseUrl(cfg) + "/collections/" + cfg.getCollectionName() + "/points";
        Map<String, Object> point = new HashMap<>();
        point.put("id", id);
        point.put("vector", vector);
        Map<String, Object> payload = new HashMap<>();
        payload.put("text", text);
        payload.put("batchId", activeBatchId);
        point.put("payload", payload);
        Map<String, Object> body = new HashMap<>();
        body.put("points", List.of(point));
        try {
            restTemplate.put(url, new HttpEntity<>(body, qdrantHeaders(cfg)));
        } catch (Exception ex) {
            log.warn("Qdrant upsert failed, fallback local search only: {}", ex.getMessage());
        }
    }

    private List<String> searchFromQdrant(float[] queryVector, int limit) {
        AgentProperties.Qdrant cfg = properties.getQdrant();
        if (cfg == null || !StringUtils.hasText(cfg.getHost()) || !StringUtils.hasText(cfg.getCollectionName())) {
            return Collections.emptyList();
        }
        String url = qdrantBaseUrl(cfg) + "/collections/" + cfg.getCollectionName() + "/points/search";
        Map<String, Object> body = new HashMap<>();
        body.put("vector", queryVector);
        body.put("limit", limit);
        body.put("with_payload", true);
        body.put("filter", qdrantBatchFilter(activeBatchId));
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, new HttpEntity<>(body, qdrantHeaders(cfg)), Map.class);
            if (response == null) {
                return Collections.emptyList();
            }
            Object result = response.get("result");
            if (!(result instanceof List<?> points)) {
                return Collections.emptyList();
            }
            List<String> docs = new ArrayList<>();
            for (Object item : points) {
                if (!(item instanceof Map<?, ?> pointMap)) {
                    continue;
                }
                Object payloadObj = pointMap.get("payload");
                if (!(payloadObj instanceof Map<?, ?> payload)) {
                    continue;
                }
                Object text = payload.get("text");
                if (text != null && StringUtils.hasText(String.valueOf(text))) {
                    docs.add(String.valueOf(text));
                }
            }
            return docs;
        } catch (Exception ex) {
            log.warn("Qdrant search failed, fallback local search: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private void ensureQdrantCollection(AgentProperties.Qdrant cfg, int vectorSize) {
        String url = qdrantBaseUrl(cfg) + "/collections/" + cfg.getCollectionName();
        Map<String, Object> vectors = new HashMap<>();
        vectors.put("size", vectorSize);
        vectors.put("distance", "Cosine");
        Map<String, Object> body = new HashMap<>();
        body.put("vectors", vectors);
        try {
            restTemplate.put(url, new HttpEntity<>(body, qdrantHeaders(cfg)));
        } catch (Exception ex) {
            log.debug("Qdrant ensure collection skipped: {}", ex.getMessage());
        }
    }

    private HttpHeaders qdrantHeaders(AgentProperties.Qdrant cfg) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (cfg != null && StringUtils.hasText(cfg.getApiKey())) {
            headers.add("api-key", cfg.getApiKey().trim());
        }
        return headers;
    }

    private String qdrantBaseUrl(AgentProperties.Qdrant cfg) {
        String protocol = cfg.isUseTls() ? "https" : "http";
        return protocol + "://" + cfg.getHost().trim() + ":" + cfg.getPort();
    }

    private Map<String, Object> qdrantBatchFilter(String batchId) {
        Map<String, Object> match = new HashMap<>();
        match.put("value", batchId);
        Map<String, Object> condition = new HashMap<>();
        condition.put("key", "batchId");
        condition.put("match", match);
        Map<String, Object> filter = new HashMap<>();
        filter.put("must", List.of(condition));
        return filter;
    }

    private List<String> topLocalTexts(int limit) {
        List<String> result = new ArrayList<>();
        synchronized (localKnowledge) {
            for (String text : localKnowledge) {
                result.add(text);
                if (result.size() >= limit) {
                    break;
                }
            }
        }
        return result;
    }

    private String findLocalTextById(String id) {
        return id == null ? null : localTextById.get(id);
    }

    private double cosineSimilarity(float[] left, float[] right) {
        int len = Math.min(left.length, right.length);
        if (len == 0) {
            return -1d;
        }
        double dot = 0d;
        double leftNorm = 0d;
        double rightNorm = 0d;
        for (int i = 0; i < len; i++) {
            dot += left[i] * right[i];
            leftNorm += left[i] * left[i];
            rightNorm += right[i] * right[i];
        }
        if (leftNorm == 0d || rightNorm == 0d) {
            return -1d;
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private static final class ScoredText {
        private final String id;
        private final double score;

        private ScoredText(String id, double score) {
            this.id = id;
            this.score = score;
        }
    }
}
