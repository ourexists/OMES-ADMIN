package com.ourexists.omes.process.engine.recipe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 从 classpath {@code process-recipes/*.yml} 加载工艺配方。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessRecipeCatalog {

    private final ObjectMapper objectMapper;

    @Getter
    private final Map<String, ProcessRecipeYamlSpec> recipes = new LinkedHashMap<>();

    /** 工序名称（归一化）→ 配方 recipeId */
    private final Map<String, String> nameIndex = new LinkedHashMap<>();

    @PostConstruct
    void loadClasspathRecipes() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath:process-recipes/*.yml");
            Yaml yaml = new Yaml();
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (!StringUtils.hasText(filename)) {
                    continue;
                }
                String fileKey = filename.replaceFirst("\\.ya?ml$", "");
                try (InputStream in = resource.getInputStream()) {
                    Object loaded = yaml.load(in);
                    mergeLoaded(fileKey, loaded);
                } catch (Exception ex) {
                    log.warn("工艺配方加载失败 {}: {}", filename, ex.getMessage());
                }
            }
            rebuildNameIndex();
            log.info("已加载工艺配方 {} 个: {}，名称索引 {} 条",
                    recipes.size(), recipes.keySet(), nameIndex.size());
        } catch (Exception ex) {
            log.warn("扫描工艺配方目录失败: {}", ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void mergeLoaded(String fileKey, Object loaded) {
        if (!(loaded instanceof Map<?, ?> root)) {
            return;
        }
        Object process = root.get("process");
        if (!(process instanceof Map<?, ?> processMap)) {
            return;
        }
        Object recipesNode = processMap.get("recipes");
        if (!(recipesNode instanceof Map<?, ?> recipesMap)) {
            return;
        }
        for (Map.Entry<?, ?> entry : recipesMap.entrySet()) {
            String recipeId = String.valueOf(entry.getKey());
            if (!StringUtils.hasText(recipeId)) {
                continue;
            }
            ProcessRecipeYamlSpec spec = objectMapper.convertValue(entry.getValue(), ProcessRecipeYamlSpec.class);
            if (spec == null) {
                continue;
            }
            if (!StringUtils.hasText(spec.getName())) {
                spec.setName(recipeId);
            }
            recipes.put(recipeId, spec);
            log.debug("注册工艺配方 id={} file={}", recipeId, fileKey);
        }
    }

    public ProcessRecipeYamlSpec require(String recipeId) {
        if (!StringUtils.hasText(recipeId)) {
            throw new BusinessException("recipeId 不能为空");
        }
        ProcessRecipeYamlSpec spec = recipes.get(recipeId.trim());
        if (spec == null) {
            throw new BusinessException(404, "工艺配方不存在: " + recipeId);
        }
        return spec;
    }

    public Set<String> recipeIds() {
        return Collections.unmodifiableSet(recipes.keySet());
    }

    /**
     * 按工序名称匹配引擎模板（与配方 YAML 中 {@code name} 比对，忽略空白差异）。
     */
    public Optional<String> findRecipeIdByStepName(String stepName) {
        String key = normalizeStepName(stepName);
        if (!StringUtils.hasText(key)) {
            return Optional.empty();
        }
        return Optional.ofNullable(nameIndex.get(key));
    }

    public ProcessRecipeYamlSpec findByStepName(String stepName) {
        return findRecipeIdByStepName(stepName)
                .map(recipes::get)
                .orElse(null);
    }

    private void rebuildNameIndex() {
        nameIndex.clear();
        for (Map.Entry<String, ProcessRecipeYamlSpec> entry : recipes.entrySet()) {
            String recipeId = entry.getKey();
            ProcessRecipeYamlSpec spec = entry.getValue();
            String nameKey = normalizeStepName(spec.getName());
            if (!StringUtils.hasText(nameKey)) {
                continue;
            }
            if (nameIndex.containsKey(nameKey)) {
                log.warn("工艺配方名称重复「{}」: {} 与 {}", nameKey, nameIndex.get(nameKey), recipeId);
            }
            nameIndex.put(nameKey, recipeId);
        }
    }

    static String normalizeStepName(String name) {
        if (!StringUtils.hasText(name)) {
            return "";
        }
        return name.replaceAll("\\s+", "")
                .replaceAll("[^\\p{L}\\p{N}]", "")
                .trim();
    }
}
