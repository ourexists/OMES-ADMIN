package com.ourexists.omes.ai.agent.loader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AgentChainConfig {

    private String chainName = "default-agent-chain";
    private String entry = "dispatch-agent";
    private List<ChainNodeConfig> nodes = new ArrayList<>();

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public List<ChainNodeConfig> getNodes() {
        return nodes;
    }

    public void setNodes(List<ChainNodeConfig> nodes) {
        this.nodes = nodes;
    }

    public static class ChainNodeConfig {
        private String id;
        private String type = "agent";
        private boolean enabled = true;
        private String provider = "openai";
        private String model = "gpt-4o-mini";
        private String prompt = "";
        private String next;
        private Map<String, String> routes = new LinkedHashMap<>();
        private List<String> tools = new ArrayList<>();
        private String memory = "stateless";
        private String outputFormat = "text";
        private String vectorStore = "qdrant";
        private String collection = "omes_inspection_kb";
        private String embeddingModel = "text-embedding-3-small";
        private Integer topK = 5;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getNext() {
            return next;
        }

        public void setNext(String next) {
            this.next = next;
        }

        public Map<String, String> getRoutes() {
            return routes;
        }

        public void setRoutes(Map<String, String> routes) {
            this.routes = routes;
        }

        public List<String> getTools() {
            return tools;
        }

        public void setTools(List<String> tools) {
            this.tools = tools;
        }

        public String getMemory() {
            return memory;
        }

        public void setMemory(String memory) {
            this.memory = memory;
        }

        public String getOutputFormat() {
            return outputFormat;
        }

        public void setOutputFormat(String outputFormat) {
            this.outputFormat = outputFormat;
        }

        public String getVectorStore() {
            return vectorStore;
        }

        public void setVectorStore(String vectorStore) {
            this.vectorStore = vectorStore;
        }

        public String getCollection() {
            return collection;
        }

        public void setCollection(String collection) {
            this.collection = collection;
        }

        public String getEmbeddingModel() {
            return embeddingModel;
        }

        public void setEmbeddingModel(String embeddingModel) {
            this.embeddingModel = embeddingModel;
        }

        public Integer getTopK() {
            return topK;
        }

        public void setTopK(Integer topK) {
            this.topK = topK;
        }
    }
}
