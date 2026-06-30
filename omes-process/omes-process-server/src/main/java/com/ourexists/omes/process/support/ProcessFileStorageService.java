/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.process.support;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.process.model.ProcessStoredFileVo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessFileStorageService {

    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final ProcessFileProperties fileProperties;
    private Path storageRoot;

    @PostConstruct
    void initStorage() throws IOException {
        storageRoot = Paths.get(fileProperties.getRootPath()).toAbsolutePath().normalize();
        Files.createDirectories(storageRoot);
    }

    public ProcessStoredFileVo storeFromBytes(byte[] bytes, String originalName, String contentType,
                                              String bizType, String remark) {
        if (bytes == null || bytes.length == 0) {
            throw new BusinessException("上传文件不能为空");
        }
        String safeName = StringUtils.cleanPath(
                StringUtils.hasText(originalName) ? originalName : "unnamed");
        if (safeName.contains("..")) {
            throw new BusinessException("文件名不合法");
        }
        String ext = extractExtension(safeName);
        String storedName = UUID.randomUUID().toString().replace("-", "") + ext;
        String dir = bizType + "/" + DATE_DIR.format(LocalDate.now());
        Path targetDir = storageRoot.resolve(dir.replace('/', java.io.File.separatorChar)).normalize();
        if (!targetDir.startsWith(storageRoot)) {
            throw new BusinessException("非法存储目录");
        }
        try {
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(storedName);
            Files.write(targetFile, bytes);
            ProcessStoredFileVo vo = new ProcessStoredFileVo();
            vo.setStoragePath(storageRoot.relativize(targetFile).toString().replace('\\', '/'));
            return vo;
        } catch (IOException e) {
            throw new BusinessException("文件保存失败");
        }
    }

    public Path getStorageRoot() {
        return storageRoot;
    }

    private static String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot > 0 && dot < filename.length() - 1) {
            return filename.substring(dot).toLowerCase(Locale.ROOT);
        }
        return "";
    }
}
