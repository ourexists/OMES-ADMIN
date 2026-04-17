package com.ourexists.omes.ai.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 知识入库请求，支持结构化与非结构化。
 */
@Getter
@Setter
public class AiKnowledgeIngestRequest {

    /**
     * structured | unstructured
     */
    private String knowledgeType;

    /**
     * 知识来源名称（文件名/系统名）
     */
    private String sourceName;

    /**
     * 非结构化文本内容
     */
    private String textContent;

    /**
     * 结构化内容（JSON字符串，支持对象或数组）
     */
    private String structuredContent;
}
