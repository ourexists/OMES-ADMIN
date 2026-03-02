package com.ourexists.omes.portal.file;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class LocalFileWebConfig implements WebMvcConfigurer {

    private final FileStorageProperties properties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 本地根目录（与 LocalFileStorageService 相同）
        Path root = Paths.get(properties.getRootPath()).toAbsolutePath().normalize();

        // 比如：/files/** 映射到本地目录 file-storage/
        String location = "file:" + root.toString() + "/";

        registry.addResourceHandler(properties.getUrlPrefix() + "/**")
                .addResourceLocations(location);
    }
}