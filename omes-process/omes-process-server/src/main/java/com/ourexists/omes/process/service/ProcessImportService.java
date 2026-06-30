package com.ourexists.omes.process.service;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.process.model.ProcessStoredFileVo;
import com.ourexists.omes.process.support.ProcessFileStorageService;
import com.ourexists.omes.process.model.ProcessImportParseResult;
import com.ourexists.omes.process.model.ProcessStepItem;
import com.ourexists.omes.process.util.JLaTeXMathFormulaSupport;
import com.ourexists.omes.process.util.OfficeMathTextExtractor;
import com.ourexists.omes.process.util.ProcessCardImportSupport;
import com.ourexists.omes.process.util.ProcessPdfParser;
import com.ourexists.omes.process.util.ProcessWordParser;
import com.ourexists.omes.process.util.RoomTempCureCurveTextParser;
import com.ourexists.omes.process.util.WordImportContentSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessImportService {

    private static final String PROCESS_IMAGE_BIZ_TYPE = "process-image";

    private final ProcessFileStorageService fileService;

    public ProcessImportParseResult parseWord(MultipartFile file) {
        validateWordFile(file);
        return parseInternal(file, ImportFormat.WORD);
    }

    public ProcessImportParseResult parsePdf(MultipartFile file) {
        validatePdfFile(file);
        return parseInternal(file, ImportFormat.PDF);
    }

    private enum ImportFormat {
        WORD, PDF
    }

    private ProcessImportParseResult parseInternal(MultipartFile file, ImportFormat format) {
        try {
            ProcessImportParseResult result = switch (format) {
                case WORD -> ProcessWordParser.parse(file.getInputStream());
                case PDF -> ProcessPdfParser.parse(file.getInputStream());
            };
            if (!StringUtils.hasText(result.getProcessCode())) {
                throw new BusinessException("未能识别工艺编号，请确认是否为标准模板");
            }
            if (result.getSteps() == null || result.getSteps().isEmpty()) {
                throw new BusinessException(switch (format) {
                    case WORD -> "未能从 Word 中识别出工序，请确认文件内容完整";
                    case PDF -> "未能从 PDF 中识别出工序，请确认文件内容完整";
                });
            }
            if (!StringUtils.hasText(result.getProcessName())) {
                result.setProcessName("导入工艺");
            }
            storeProcessImage(result);
            normalizeImportedDisplayText(result);
            result.setWarnings(buildWarnings(result));
            return result;
        } catch (BusinessException ex) {
            throw ex;
        } catch (ProcessPdfParser.PdfParseException ex) {
            throw new BusinessException(ex.getMessage());
        } catch (Exception ex) {
            log.warn("工艺卡片{} 解析失败", formatLabel(format), ex);
            throw new BusinessException(switch (format) {
                case WORD -> "Word 文件解析失败，请确认文件格式正确";
                case PDF -> "PDF 文件解析失败，请确认文件格式正确";
            });
        }
    }

    private static String formatLabel(ImportFormat format) {
        return switch (format) {
            case WORD -> "Word";
            case PDF -> "PDF";
        };
    }

    private void storeProcessImage(ProcessImportParseResult result) {
        byte[] bytes = result.getProcessImageBytes();
        if (bytes == null || bytes.length == 0) {
            return;
        }
        try {
            String ext = StringUtils.hasText(result.getProcessImageExtension())
                    ? result.getProcessImageExtension() : ".png";
            if (!ext.startsWith(".")) {
                ext = "." + ext;
            }
            String code = StringUtils.hasText(result.getProcessCode()) ? result.getProcessCode() : "process";
            String originalName = code + "-diagram" + ext;
            String contentType = StringUtils.hasText(result.getProcessImageContentType())
                    ? result.getProcessImageContentType() : MediaType.IMAGE_PNG_VALUE;
            ProcessStoredFileVo file = fileService.storeFromBytes(bytes, originalName, contentType,
                    PROCESS_IMAGE_BIZ_TYPE, "工艺卡片导入简图");
            result.setProcessImageUrl(file.getStoragePath());
        } catch (BusinessException ex) {
            log.warn("工艺简图保存失败: {}", ex.getMessage());
        } catch (Exception ex) {
            log.warn("工艺简图保存失败", ex);
        } finally {
            result.setProcessImageBytes(null);
            result.setProcessImageExtension(null);
            result.setProcessImageContentType(null);
        }
    }

    private void validateWordFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请上传 Word 文件");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename)) {
            throw new BusinessException("请上传 Word 文件");
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".docx")) {
            throw new BusinessException("请上传 .docx 格式的 Word 文件");
        }
    }

    private void validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请上传 PDF 文件");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename)) {
            throw new BusinessException("请上传 PDF 文件");
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".pdf")) {
            throw new BusinessException("请上传 .pdf 格式的 PDF 文件");
        }
    }

    private void normalizeImportedDisplayText(ProcessImportParseResult result) {
        if (result.getSteps() == null) {
            return;
        }
        for (ProcessStepItem step : result.getSteps()) {
            if (StringUtils.hasText(step.getStepName())) {
                step.setStepName(OfficeMathTextExtractor.normalizeDisplayText(step.getStepName()));
            }
            if (StringUtils.hasText(step.getStepContent())) {
                step.setStepContent(normalizeImportedFormulaText(step.getStepContent()));
            }
        }
    }

    private static String normalizeImportedFormulaText(String text) {
        String display = ProcessCardImportSupport.normalizePdfDiameterGlyph(text);
        display = OfficeMathTextExtractor.normalizeDisplayText(display);
        display = WordImportContentSanitizer.stripLatexArtifacts(display);
        if (looksLikeImportedFormula(display)) {
            return OfficeMathTextExtractor.normalizeDisplayText(JLaTeXMathFormulaSupport.plainTextViaLatex(display));
        }
        return display;
    }

    private static boolean looksLikeImportedFormula(String text) {
        return text.contains("℃") || text.contains("±") || text.contains("→")
                || text.contains("┴") || text.contains("φ") || text.contains("Φ")
                || text.contains("×") || text.contains("恒温");
    }

    private ArrayList<String> buildWarnings(ProcessImportParseResult result) {
        ArrayList<String> warnings = new ArrayList<>();
        if (!StringUtils.hasText(result.getComponentCode())) {
            warnings.add("未识别到零组件号，请手动补充");
        }
        if (result.getMolds() == null || result.getMolds().isEmpty()) {
            warnings.add("未识别到压模图号，请手动补充");
        }
        if (result.getHoldTimeSeconds() == null) {
            warnings.add("未识别到保持时间，请手动补充");
        }
        if (result.getBlankWeight() == null) {
            warnings.add("未识别到毛料重量，请手动补充");
        }
        if (!StringUtils.hasText(result.getProcessImageUrl())) {
            warnings.add("未识别到工艺简图，请手动上传");
        }
        if (result.getSteps() != null) {
            for (ProcessStepItem step : result.getSteps()) {
                if (RoomTempCureCurveTextParser.isRoomTempCureStep(step.getStepName())
                        && !StringUtils.hasText(step.getParams())) {
                    warnings.add("二段硫化工序未识别到室温曲线参数（如 室温→1h80℃→…×6h恒温），请手动配置");
                    break;
                }
            }
        }
        return warnings;
    }
}
