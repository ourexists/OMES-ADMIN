/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.mo.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.mat.feign.MATFeign;
import com.ourexists.omes.mat.model.MaterialDto;
import com.ourexists.omes.mo.enums.MOStatusEnum;
import com.ourexists.omes.mo.enums.MoAdjustSourceEnum;
import com.ourexists.omes.mo.enums.MoAdjustTypeEnum;
import com.ourexists.omes.mo.feign.MOFeign;
import com.ourexists.omes.mo.model.MODetailDto;
import com.ourexists.omes.mo.model.MODto;
import com.ourexists.omes.mo.model.MoAdjustCommand;
import com.ourexists.omes.mo.model.MoAdjustLogDto;
import com.ourexists.omes.mo.model.MoAdjustResult;
import com.ourexists.omes.mo.model.query.MOPageQuery;
import com.ourexists.omes.mps.enums.MPSStatusEnum;
import com.ourexists.omes.mps.feign.MPSFeign;
import com.ourexists.omes.mps.model.MPSDto;
import com.ourexists.omes.portal.flow.MoAdjustOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "生产订单")
@RestController
@RequestMapping("/mo")
public class MOController {

    @Autowired
    private MOFeign moFeign;

    @Autowired
    private MATFeign matFeign;

    @Autowired
    private MPSFeign mpsFeign;

    @Autowired
    private MoAdjustOrchestrator moAdjustOrchestrator;

    @Operation(summary = "分页", description = "")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<MODto>> selectByPage(@RequestBody MOPageQuery dto) {
        return moFeign.selectByPage(dto);
    }

    @Operation(summary = "通过id查询所有", description = "")
    @GetMapping("selectById")
    public JsonResponseEntity<MODto> selectById(@RequestParam String id) {
        return moFeign.selectById(id);
    }

    @Operation(summary = "新增或修改根据id", description = "")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody MODto dto) {
        if (CollectionUtil.isNotBlank(dto.getDetailDtoList())) {
            dto.getDetailDtoList().forEach(e -> e.setId(null));
            List<String> matCodes = dto.getDetailDtoList().stream().map(MODetailDto::getMatCode).distinct().collect(Collectors.toList());
            IdsDto idsDto = new IdsDto();
            idsDto.setIds(matCodes);
            try {
                List<MaterialDto> mats = RemoteHandleUtils.getDataFormResponse(matFeign.selectByCodes(idsDto));
                List<MaterialDto> exists = new ArrayList<>();
                for (MaterialDto mat : mats) {
                    for (MODetailDto detailDto : dto.getDetailDtoList()) {
                        if (mat.getSelfCode().equals(detailDto.getMatCode())) {
                            detailDto.setMatId(mat.getId());
                            exists.add(mat);
                        }
                    }
                }
                mats.removeAll(exists);
                if (CollectionUtil.isNotBlank(mats)) {
                    StringBuilder error = new StringBuilder();
                    for (MaterialDto mat : mats) {
                        error.append(mat.getSelfCode()).append("-").append(mat.getName()).append("|");
                    }
                    throw new BusinessException("${mo.mat.nomatch}", error.substring(0, error.length() - 1));
                }
            } catch (EraCommonException e) {
                throw new RuntimeException(e);
            }
        }
        return moFeign.addOrUpdate(dto);
    }

    /**
     * 仅允许清理 INIT 且无子 MPS（或子 MPS 已全部 CANCEL）的草稿。
     * 业务取消请使用 {@link #adjust(MoAdjustCommand)}。
     */
    @Operation(summary = "删除草稿", description = "仅 INIT 且无有效子计划；业务取消请 POST /mo/adjust")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        if (idsDto == null || CollectionUtil.isBlank(idsDto.getIds())) {
            return JsonResponseEntity.success(true);
        }
        try {
            for (String id : idsDto.getIds()) {
                MODto mo = RemoteHandleUtils.getDataFormResponse(moFeign.selectById(id));
                if (mo == null) {
                    continue;
                }
                if (!MOStatusEnum.INIT.getCode().equals(mo.getStatus())) {
                    throw new BusinessException("${mo.msg.delete.only.init}");
                }
                List<MPSDto> mpsList = RemoteHandleUtils.getDataFormResponse(mpsFeign.selectByMoCode(mo.getSelfCode()));
                if (CollectionUtil.isNotBlank(mpsList)) {
                    boolean hasActive = mpsList.stream().anyMatch(m ->
                            m.getStatus() != null && !MPSStatusEnum.CANCEL.getCode().equals(m.getStatus()));
                    if (hasActive) {
                        throw new BusinessException("${mo.msg.delete.has.mps}");
                    }
                }
            }
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        return moFeign.delete(idsDto);
    }

    @Operation(summary = "生产订单调整", description = "统一入口：CANCEL_*/RESCHEDULE/PRIORITY/CHANGE_LINE/CHANGE_DEV/QTY_*")
    @PostMapping("adjust")
    public JsonResponseEntity<MoAdjustResult> adjust(@Validated @RequestBody MoAdjustCommand command) {
        if (StringUtils.isBlank(command.getRequestId())) {
            command.setRequestId(IdWorker.getIdStr());
        }
        if (StringUtils.isBlank(command.getSource())) {
            command.setSource(MoAdjustSourceEnum.UI.name());
        }
        return JsonResponseEntity.success(moAdjustOrchestrator.adjust(command));
    }

    @Operation(summary = "调整预览", description = "只算影响面，不落库")
    @PostMapping("adjust/preview")
    public JsonResponseEntity<com.ourexists.omes.mo.model.MoAdjustPreviewResult> adjustPreview(
            @Validated @RequestBody MoAdjustCommand command) {
        if (StringUtils.isBlank(command.getRequestId())) {
            command.setRequestId("preview-" + IdWorker.getIdStr());
        }
        if (StringUtils.isBlank(command.getSource())) {
            command.setSource(MoAdjustSourceEnum.UI.name());
        }
        return JsonResponseEntity.success(moAdjustOrchestrator.preview(command));
    }

    @Operation(summary = "调整审计日志", description = "按 moCode 查询")
    @GetMapping("adjust/logs")
    public JsonResponseEntity<List<MoAdjustLogDto>> adjustLogs(@RequestParam String moCode) {
        return JsonResponseEntity.success(moAdjustOrchestrator.listLogs(moCode));
    }

    @Operation(summary = "surplus 对账", description = "检查 num == surplus + active MPS")
    @GetMapping("adjust/reconcile")
    public JsonResponseEntity<List<com.ourexists.omes.mo.model.MoSurplusReconcileItem>> reconcile(
            @RequestParam String moCode) {
        return JsonResponseEntity.success(moAdjustOrchestrator.reconcile(moCode));
    }

    @Operation(summary = "状态", description = "状态")
    @GetMapping("status")
    public JsonResponseEntity<List<MapDto>> status() {
        List<MapDto> r = new ArrayList<>();
        for (MOStatusEnum value : MOStatusEnum.values()) {
            r.add(new MapDto().setId(value.getCode().toString()).setName(value.getName()));
        }
        return JsonResponseEntity.success(r);
    }

    /**
     * 便捷：整单取消（内部转 adjust）
     */
    @Operation(summary = "取消生产订单", description = "delegating → /mo/adjust CANCEL_MO")
    @PostMapping("cancel")
    public JsonResponseEntity<MoAdjustResult> cancel(@RequestBody Map<String, Object> body) {
        String moCode = body == null ? null : String.valueOf(body.get("moCode"));
        if (StringUtils.isBlank(moCode) || "null".equals(moCode)) {
            throw new BusinessException("${mo.adjust.moCode.required}");
        }
        boolean force = body.get("force") != null && Boolean.parseBoolean(String.valueOf(body.get("force")));
        MoAdjustCommand cmd = new MoAdjustCommand()
                .setMoCode(moCode)
                .setAdjustType(MoAdjustTypeEnum.CANCEL_MO.name())
                .setSource(MoAdjustSourceEnum.UI.name())
                .setRequestId(body.get("requestId") != null
                        ? String.valueOf(body.get("requestId"))
                        : IdWorker.getIdStr())
                .setOperator(body.get("operator") != null ? String.valueOf(body.get("operator")) : null)
                .setForce(force)
                .setPayload(Collections.emptyMap());
        return adjust(cmd);
    }

}
