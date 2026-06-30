package com.ourexists.omes.process.engine.support;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "process")
public class ProcessAviatorRuleProperties {

    private Map<String, String> condition = new LinkedHashMap<>();
}
