package com.ourexists.omes.process.engine;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 工序流程引擎配置加载策略。
 */
@Data
@Component
@ConfigurationProperties(prefix = "process.engine")
public class ProcessEngineProperties {

    /**
     * {@code file}：按工序名称匹配 classpath {@code process-recipes/*.yml} 模板，并结合
     * {@code params} / {@code stepScript} 现场编译，不使用库表 {@code step_engine_config}；
     * {@code database}：执行时优先读取工序表 {@code step_engine_config} 字段（保存工艺时写入）。
     */
    private ConfigSource configSource = ConfigSource.DATABASE;

    public boolean isFileSource() {
        return configSource == ConfigSource.FILE;
    }

    public boolean isDatabaseSource() {
        return configSource == ConfigSource.DATABASE;
    }

    public enum ConfigSource {
        FILE,
        DATABASE
    }
}
