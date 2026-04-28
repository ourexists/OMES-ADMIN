package com.ourexists.omes.ai.agent.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AgentChainConfigLoader {
    private final ObjectMapper objectMapper;
    private final String configPath;

    public AgentChainConfigLoader(ObjectMapper objectMapper,
                                  @Value("${agent.multi-agent.runtime-config-path:config/ai-agent-chain.json}") String configPath) {
        this.objectMapper = objectMapper;
        this.configPath = configPath;
    }

    public AgentChainConfig load() {
        try {
            return normalize(readConfig());
        } catch (Exception ex) {
            log.warn("Load agent chain config failed, fallback to default: {}", ex.getMessage());
            return normalize(defaultConfig());
        }
    }

    public String resolveOrchestratorModel(AgentChainConfig config) {
        if (config == null || config.getNodes() == null) {
            return "gpt-4o-mini";
        }
        for (AgentChainConfig.ChainNodeConfig node : config.getNodes()) {
            if (node != null && "llm".equalsIgnoreCase(node.getType()) && node.getModel() != null && !node.getModel().isBlank()) {
                return node.getModel().trim();
            }
        }
        return "gpt-4o-mini";
    }

    public List<String> enabledAgents(AgentChainConfig config) {
        if (config == null || config.getNodes() == null) {
            return List.of("dispatch-agent", "device-agent", "data-agent", "alarm-agent");
        }
        List<String> result = new ArrayList<>();
        for (AgentChainConfig.ChainNodeConfig node : config.getNodes()) {
            if (node != null
                    && node.isEnabled()
                    && node.getId() != null
                    && !node.getId().isBlank()
                    && ("agent".equalsIgnoreCase(node.getType()) || "rag".equalsIgnoreCase(node.getType()))) {
                result.add(node.getId());
            }
        }
        if (result.isEmpty()) {
            return List.of("dispatch-agent", "device-agent", "data-agent", "alarm-agent");
        }
        return result;
    }

    public String resolveNodeModel(AgentChainConfig config, String agentId) {
        if (config == null || config.getNodes() == null || agentId == null) {
            return "gpt-4o-mini";
        }
        AgentChainConfig.ChainNodeConfig agent = findNode(config, agentId);
        if (agent != null) {
            String model = agent.getModel();
            return (model == null || model.isBlank()) ? "gpt-4o-mini" : model.trim();
        }
        return "gpt-4o-mini";
    }

    public String resolveNodeProvider(AgentChainConfig config, String agentId) {
        if (config == null || config.getNodes() == null || agentId == null) {
            return "openai";
        }
        AgentChainConfig.ChainNodeConfig agent = findNode(config, agentId);
        if (agent != null) {
            String provider = agent.getProvider();
            return (provider == null || provider.isBlank()) ? "openai" : provider.trim().toLowerCase();
        }
        return "openai";
    }

    public AgentChainConfig.ChainNodeConfig findNode(AgentChainConfig config, String nodeId) {
        if (config == null || config.getNodes() == null || nodeId == null) {
            return null;
        }
        for (AgentChainConfig.ChainNodeConfig node : config.getNodes()) {
            if (node != null && nodeId.equals(node.getId())) {
                return node;
            }
        }
        return null;
    }

    private AgentChainConfig readConfig() throws Exception {
        Path path = Path.of(configPath);
        if (Files.exists(path)) {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, AgentChainConfig.class);
        }
        ClassPathResource resource = new ClassPathResource("ai-agent-chain.json");
        if (resource.exists()) {
            String json = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return objectMapper.readValue(json, AgentChainConfig.class);
        }
        return defaultConfig();
    }

    private AgentChainConfig defaultConfig() {
        AgentChainConfig config = new AgentChainConfig();
        config.setChainName("equipment-dispatch-hub");
        config.setEntry("dispatch-agent");
        config.setNodes(new ArrayList<>());
        return config;
    }

    private AgentChainConfig normalize(AgentChainConfig config) {
        if (config == null) {
            config = new AgentChainConfig();
        }
        if (config.getChainName() == null || config.getChainName().isBlank()) {
            config.setChainName("default-agent-chain");
        }
        if (config.getEntry() == null || config.getEntry().isBlank()) {
            config.setEntry("dispatch-agent");
        }
        if (config.getNodes() == null) {
            config.setNodes(new ArrayList<>());
        }
        Map<String, AgentChainConfig.ChainNodeConfig> nodeMap = new LinkedHashMap<>();
        for (AgentChainConfig.ChainNodeConfig item : config.getNodes()) {
            if (item == null || item.getId() == null || item.getId().isBlank()) {
                continue;
            }
            nodeMap.put(item.getId(), item);
        }
        ensureNode(nodeMap, "dispatch-agent", "agent", "gpt-4o-mini", null);
        ensureNode(nodeMap, "device-agent", "agent", "gpt-4o-mini", "dispatch-agent");
        ensureNode(nodeMap, "data-agent", "agent", "gpt-4o-mini", "dispatch-agent");
        ensureNode(nodeMap, "alarm-agent", "agent", "gpt-4o-mini", "dispatch-agent");
        ensureNode(nodeMap, "router", "llm-router", "gpt-4o-mini", null);
        ensureNode(nodeMap, "final", "llm", "gpt-4o-mini", null);
        AgentChainConfig.ChainNodeConfig finalNode = nodeMap.get("final");
        if (finalNode != null && finalNode.getNext() == null && "llm".equalsIgnoreCase(finalNode.getType())) {
            finalNode.setEnabled(false);
        }
        config.setNodes(new ArrayList<>(nodeMap.values()));
        return config;
    }

    private void ensureNode(Map<String, AgentChainConfig.ChainNodeConfig> nodeMap,
                            String id,
                            String defaultType,
                            String defaultModel,
                            String defaultNext) {
        AgentChainConfig.ChainNodeConfig node = nodeMap.get(id);
        if (node == null) {
            node = new AgentChainConfig.ChainNodeConfig();
            node.setId(id);
            node.setEnabled(true);
            node.setType(defaultType);
            node.setProvider("openai");
            node.setModel(defaultModel);
            node.setNext(defaultNext);
            nodeMap.put(id, node);
            return;
        }
        if (node.getType() == null || node.getType().isBlank()) {
            node.setType(defaultType);
        }
        if (node.getModel() == null || node.getModel().isBlank()) {
            node.setModel(defaultModel);
        }
        if (node.getProvider() == null || node.getProvider().isBlank()) {
            node.setProvider("openai");
        }
        if ((node.getNext() == null || node.getNext().isBlank()) && defaultNext != null) {
            node.setNext(defaultNext);
        }
    }

}
