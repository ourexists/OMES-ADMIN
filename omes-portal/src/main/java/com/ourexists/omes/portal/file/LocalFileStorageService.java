package com.ourexists.omes.portal.file;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LocalFileStorageService {

    @Autowired
    private FileStorageProperties fileStorageProperties;

    private Path rootLocation;

    @PostConstruct
    public void init() throws IOException {
        this.rootLocation = Paths.get(fileStorageProperties.getRootPath()).toAbsolutePath().normalize();
        Files.createDirectories(this.rootLocation);
    }

    /** 保存文件到指定子目录（可为空） */
    public String store(MultipartFile file, String dir) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }
        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalName.contains("..")) {
            throw new IllegalArgumentException("非法文件名: " + originalName);
        }

        Path targetDir = resolveSafeDir(dir);
        Files.createDirectories(targetDir);

        Path targetFile = targetDir.resolve(originalName).normalize();
        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

        // 返回相对路径（供前端或下载时使用）
        return this.rootLocation.relativize(targetFile).toString().replace("\\", "/");
    }

    /** 按相对路径加载为 Resource 用于下载 */
    public Resource loadAsResource(String relativePath) throws MalformedURLException {
        Path file = resolveSafePath(relativePath);
        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalArgumentException("文件不存在或不可读: " + relativePath);
        }
        return resource;
    }

    /** 删除文件 */
    public void delete(String relativePath) throws IOException {
        Path file = resolveSafePath(relativePath);
        Files.deleteIfExists(file);
    }

    /** 列出某个目录下的文件（相对路径） */
    public List<String> list(String dir) throws IOException {
        Path targetDir = resolveSafeDir(dir);
        if (!Files.exists(targetDir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(targetDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(p -> this.rootLocation.relativize(p).toString().replace("\\", "/"))
                    .collect(Collectors.toList());
        }
    }

    private Path resolveSafeDir(String dir) {
        if (!StringUtils.hasText(dir)) {
            return this.rootLocation;
        }
        Path target = this.rootLocation.resolve(dir).normalize();
        if (!target.startsWith(this.rootLocation)) {
            throw new IllegalArgumentException("非法目录: " + dir);
        }
        return target;
    }

    private Path resolveSafePath(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new IllegalArgumentException("相对路径不能为空");
        }
        Path target = this.rootLocation.resolve(relativePath).normalize();
        if (!target.startsWith(this.rootLocation)) {
            throw new IllegalArgumentException("非法路径: " + relativePath);
        }
        return target;
    }
}
