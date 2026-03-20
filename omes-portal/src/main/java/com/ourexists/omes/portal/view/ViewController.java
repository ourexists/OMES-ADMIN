/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.view;

import org.springframework.stereotype.Controller;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ViewController {
    private final ResourceLoader resourceLoader;

    public ViewController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/view/{viewName}")
    public String view(@PathVariable String viewName) {
        // 默认 Thymeleaf 模板查找位置：classpath:/templates/{viewName}.html
        String templatePath = "classpath:/templates/" + viewName + ".html";
        if (resourceLoader.getResource(templatePath).exists()) {
            return viewName;
        }
        // 走 Spring Boot 的默认错误处理（避免找不到模板导致 500）
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "View template not found: " + viewName);
    }
}
