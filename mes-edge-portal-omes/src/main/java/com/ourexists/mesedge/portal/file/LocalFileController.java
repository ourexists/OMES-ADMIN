package com.ourexists.mesedge.portal.file;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "本地文件管理")
@RestController
@RequestMapping("/localFile")
@RequiredArgsConstructor
public class LocalFileController {

    private final LocalFileStorageService storageService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public JsonResponseEntity<String> upload(@RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "dir", required = false) String dir) throws IOException {
        String relativePath = storageService.store(file, dir);
        return JsonResponseEntity.success(relativePath);
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("path") String path,
                                             HttpServletRequest request) throws MalformedURLException {
        Resource resource = storageService.loadAsResource(path);
        String filename = resource.getFilename();
        String encodedName = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ignored) {
        }
        if (!StringUtils.hasText(contentType)) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName)
                .body(resource);
    }

    @Operation(summary = "列出目录下文件")
    @GetMapping("/list")
    public JsonResponseEntity<List<String>> list(@RequestParam(value = "dir", required = false) String dir) throws IOException {
        return JsonResponseEntity.success(storageService.list(dir));
    }

    @Operation(summary = "删除文件")
    @PostMapping("/delete")
    public JsonResponseEntity<Boolean> delete(@RequestParam("path") String path) throws IOException {
        storageService.delete(path);
        return JsonResponseEntity.success(true);
    }
}
