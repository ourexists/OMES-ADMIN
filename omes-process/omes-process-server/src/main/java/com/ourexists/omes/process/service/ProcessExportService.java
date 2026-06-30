package com.ourexists.omes.process.service;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.process.support.ProcessFileProperties;
import com.ourexists.omes.process.model.ProcessIdRequest;
import com.ourexists.omes.process.model.ProcessVO;
import com.ourexists.omes.process.util.ProcessCardDiagramImageSupport;
import com.ourexists.omes.process.util.ProcessCardFormatUtil;
import com.ourexists.omes.process.util.ProcessCardPdfExporter;
import com.ourexists.omes.process.util.ProcessWordExporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessExportService {

    private final BizProcessService processService;
    private final ProcessFileProperties fileProperties;

    public ResponseEntity<byte[]> exportPdf(ProcessIdRequest request) {
        long started = System.currentTimeMillis();
        try {
            ProcessVO process = processService.detail(request.getId());
            byte[] imageBytes = loadProcessImageBytes(process.getProcessImageUrl());
            byte[] body = ProcessCardPdfExporter.export(process, imageBytes);
            String filename = ProcessCardFormatUtil.safeFilename(
                    process.getProcessCode(), process.getProcessName(), ".pdf");
            log.info("导出工艺 PDF 成功 id={} pages={} bytes={} costMs={}",
                    request.getId(), process.getSteps() != null ? process.getSteps().size() : 0,
                    body.length, System.currentTimeMillis() - started);
            return buildAttachment(body, filename, MediaType.APPLICATION_PDF_VALUE);
        } catch (BusinessException e) {
            throw e;
        } catch (IllegalStateException e) {
            log.warn("导出工艺 PDF 拒绝 id={} reason={}", request.getId(), e.getMessage());
            throw new BusinessException(e.getMessage());
        } catch (OutOfMemoryError e) {
            log.error("导出工艺 PDF 内存不足 id={} costMs={}", request.getId(),
                    System.currentTimeMillis() - started, e);
            throw new BusinessException("工序内容过多导致内存不足，请精简后导出或改用 Word");
        } catch (Exception e) {
            log.error("导出工艺 PDF 失败 id={} costMs={}", request.getId(),
                    System.currentTimeMillis() - started, e);
            throw new BusinessException("PDF 导出失败，请稍后重试或改用 Word 导出");
        }
    }

    public ResponseEntity<byte[]> exportWord(ProcessIdRequest request) {
        long started = System.currentTimeMillis();
        try {
            ProcessVO process = processService.detail(request.getId());
            byte[] imageBytes = loadProcessImageBytes(process.getProcessImageUrl());
            byte[] body = ProcessWordExporter.export(process, imageBytes);
            String filename = ProcessCardFormatUtil.safeFilename(
                    process.getProcessCode(), process.getProcessName(), ".docx");
            log.info("导出工艺 Word 成功 id={} bytes={} costMs={}",
                    request.getId(), body.length, System.currentTimeMillis() - started);
            return buildAttachment(body, filename,
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        } catch (BusinessException e) {
            throw e;
        } catch (IllegalStateException e) {
            log.warn("导出工艺 Word 拒绝 id={} reason={}", request.getId(), e.getMessage());
            throw new BusinessException(e.getMessage());
        } catch (OutOfMemoryError e) {
            log.error("导出工艺 Word 内存不足 id={} costMs={}", request.getId(),
                    System.currentTimeMillis() - started, e);
            throw new BusinessException("工序内容过多导致内存不足，请精简后导出");
        } catch (Exception e) {
            log.error("导出工艺 Word 失败 id={} costMs={}", request.getId(),
                    System.currentTimeMillis() - started, e);
            throw new BusinessException("Word 导出失败，请稍后重试");
        }
    }

    private byte[] loadProcessImageBytes(String storagePath) {
        if (!StringUtils.hasText(storagePath)) {
            return null;
        }
        Path root = Paths.get(fileProperties.getRootPath()).toAbsolutePath().normalize();
        byte[] bytes = ProcessCardDiagramImageSupport.loadImageBytes(root, storagePath);
        if (bytes == null) {
            log.warn("工艺简图文件不存在或不可读: {}", storagePath.trim());
        }
        return bytes;
    }

    private ResponseEntity<byte[]> buildAttachment(byte[] body, String filename, String contentType) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(body);
    }
}
