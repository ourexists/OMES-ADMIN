package com.ourexists.omes.portal.file;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {

    /**
     * 本地存储根目录，可以是绝对路径或相对路径
     */
    private String rootPath = "file";

    /**
     * 对外访问前缀（可选）
     */
    private String urlPrefix = "/files";
}