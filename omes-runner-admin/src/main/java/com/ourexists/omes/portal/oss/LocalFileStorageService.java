package com.ourexists.omes.portal.oss;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
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

        return relativize(targetFile);
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

    /** 删除文件或目录（目录递归删除） */
    public void delete(String relativePath) throws IOException {
        Path target = resolveSafePath(relativePath);
        if (!Files.exists(target)) {
            return;
        }
        if (Files.isDirectory(target)) {
            try (Stream<Path> walk = Files.walk(target)) {
                walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                });
            } catch (UncheckedIOException ex) {
                throw ex.getCause();
            }
            return;
        }
        Files.deleteIfExists(target);
    }

    /** 列出某个目录下的文件（相对路径，仅普通文件） */
    public List<String> list(String dir) throws IOException {
        Path targetDir = resolveSafeDir(dir);
        if (!Files.exists(targetDir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(targetDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(this::relativize)
                    .collect(Collectors.toList());
        }
    }

    /** 浏览目录：返回当前目录下的文件与子目录 */
    public List<LocalFileEntryVo> browse(String dir) throws IOException {
        Path targetDir = resolveSafeDir(dir);
        if (!Files.exists(targetDir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(targetDir)) {
            return stream
                    .sorted((left, right) -> {
                        boolean leftDir = Files.isDirectory(left);
                        boolean rightDir = Files.isDirectory(right);
                        if (leftDir != rightDir) {
                            return leftDir ? -1 : 1;
                        }
                        return left.getFileName().toString().compareToIgnoreCase(right.getFileName().toString());
                    })
                    .map(this::toEntry)
                    .collect(Collectors.toList());
        }
    }

    /** 新建子目录 */
    public String mkdir(String dir, String name) throws IOException {
        String folderName = validateEntryName(name, "目录名");
        Path target = resolveSafeDir(dir).resolve(folderName).normalize();
        if (!target.startsWith(rootLocation)) {
            throw new IllegalArgumentException("非法目录: " + name);
        }
        Files.createDirectories(target);
        return relativize(target);
    }

    /** 重命名文件或目录（同级） */
    public String rename(String relativePath, String newName) throws IOException {
        String entryName = validateEntryName(newName, "名称");
        Path source = resolveSafePath(relativePath);
        if (!Files.exists(source)) {
            throw new IllegalArgumentException("路径不存在: " + relativePath);
        }
        Path parent = source.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("无法重命名根目录");
        }
        Path target = parent.resolve(entryName).normalize();
        if (!target.startsWith(rootLocation)) {
            throw new IllegalArgumentException("非法名称: " + newName);
        }
        if (Files.exists(target)) {
            throw new IllegalArgumentException("名称已存在: " + newName);
        }
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        return relativize(target);
    }

    private LocalFileEntryVo toEntry(Path path) {
        LocalFileEntryVo entry = new LocalFileEntryVo();
        entry.setName(path.getFileName().toString());
        entry.setPath(relativize(path));
        boolean directory = Files.isDirectory(path);
        entry.setDirectory(directory);
        try {
            entry.setLastModified(Files.getLastModifiedTime(path).toMillis());
        } catch (IOException ignored) {
            entry.setLastModified(null);
        }
        if (directory) {
            entry.setSize(null);
            entry.setExtension("");
            return entry;
        }
        try {
            entry.setSize(Files.size(path));
        } catch (IOException ignored) {
            entry.setSize(null);
        }
        String fileName = entry.getName();
        int dot = fileName.lastIndexOf('.');
        if (dot > 0 && dot < fileName.length() - 1) {
            entry.setExtension(fileName.substring(dot + 1).toLowerCase());
        } else {
            entry.setExtension("");
        }
        return entry;
    }

    private String validateEntryName(String name, String label) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException(label + "不能为空");
        }
        String trimmed = name.trim();
        if (trimmed.contains("..") || trimmed.contains("/") || trimmed.contains("\\")) {
            throw new IllegalArgumentException("非法" + label + ": " + name);
        }
        return trimmed;
    }

    private String relativize(Path path) {
        return rootLocation.relativize(path).toString().replace("\\", "/");
    }

    private Path resolveSafeDir(String dir) {
        if (!StringUtils.hasText(dir)) {
            return rootLocation;
        }
        Path target = rootLocation.resolve(dir).normalize();
        if (!target.startsWith(rootLocation)) {
            throw new IllegalArgumentException("非法目录: " + dir);
        }
        return target;
    }

    private Path resolveSafePath(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new IllegalArgumentException("相对路径不能为空");
        }
        Path target = rootLocation.resolve(relativePath).normalize();
        if (!target.startsWith(rootLocation)) {
            throw new IllegalArgumentException("非法路径: " + relativePath);
        }
        return target;
    }
}
