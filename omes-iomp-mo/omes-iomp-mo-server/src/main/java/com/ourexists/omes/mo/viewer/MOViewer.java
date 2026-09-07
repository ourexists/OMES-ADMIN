/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mo.viewer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.omes.mo.enums.MOStatusEnum;
import com.ourexists.omes.mo.enums.MoAdjustLogStatusEnum;
import com.ourexists.omes.mo.feign.MOFeign;
import com.ourexists.omes.mo.model.MODetailDto;
import com.ourexists.omes.mo.model.MODto;
import com.ourexists.omes.mo.model.MoAdjustLogDto;
import com.ourexists.omes.mo.model.query.MOPageQuery;
import com.ourexists.omes.mo.pojo.MO;
import com.ourexists.omes.mo.pojo.MODetail;
import com.ourexists.omes.mo.pojo.MoAdjustLog;
import com.ourexists.omes.mo.service.MODetailService;
import com.ourexists.omes.mo.service.MOService;
import com.ourexists.omes.mo.service.MoAdjustLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

//@Tag(name = "生产订单")
//@RestController
//@RequestMapping("/mo")
@Component
public class MOViewer implements MOFeign {

    @Autowired
    private MOService service;

    @Autowired
    private MODetailService detailService;

    @Autowired
    private MoAdjustLogService adjustLogService;

    //    @Operation(summary = "分页", description = "")
//    @PostMapping("selectByPage")
    public JsonResponseEntity<List<MODto>> selectByPage(@RequestBody MOPageQuery dto) {
        Page<MO> page = service.selectByPage(dto);
        List<MODto> r = MO.covert(page.getRecords());

        List<MODetail> details = null;
        if (dto.getQueryDetail() && CollectionUtil.isNotBlank(r)) {
            List<String> selfCodes = r.stream().map(MODto::getSelfCode).collect(Collectors.toList());
            details = detailService.list(new LambdaQueryWrapper<MODetail>().in(MODetail::getMcode, selfCodes).orderByAsc(MODetail::getId));
        }
        for (MODto rs : r) {
            if (CollectionUtil.isNotBlank(details)) {
                List<MODetailDto> dtos = new ArrayList<>();
                for (MODetail detail : details) {
                    if (rs.getSelfCode().equals(detail.getMcode())) {
                        dtos.add(MODetail.covert(detail));
                    }
                }
                rs.setDetailDtoList(dtos);
            }
        }
        return JsonResponseEntity.success(r, OrmUtils.extraPagination(page));
    }

    //    @Operation(summary = "通过id查询所有", description = "")
//    @GetMapping("selectById")
    public JsonResponseEntity<MODto> selectById(@RequestParam String id) {
        MO mo = service.getById(id);
        MODto dto = null;
        if (mo != null) {
            dto = MO.covert(mo);
            dto.setDetailDtoList(MODetail.covert(detailService.selectByMcode(dto.getSelfCode())));
        }
        return JsonResponseEntity.success(dto);
    }

    //    @Operation(summary = "新增或修改根据id", description = "")
//    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody MODto dto) {
        if (StringUtils.isBlank(dto.getSelfCode())) {
            dto.setSelfCode("MO" + com.ourexists.era.framework.core.utils.id.IdWorker.get32UUID());
        }
        try {
            service.addOrUpdate(dto);
        } catch (DataAccessException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SQLIntegrityConstraintViolationException) {
                throw new BusinessException("${valid.code.duplicate}");
            }
        }
        return JsonResponseEntity.success(true);
    }

    //    @Operation(summary = "删除", description = "")
//    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.delete(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<MODto> selectByCode(@RequestParam String moCode) {
        MO mo = service.selectByCode(moCode);
        MODto dto = null;
        if (mo != null) {
            dto = MO.covert(mo);
            dto.setDetailDtoList(MODetail.covert(detailService.selectByMcode(dto.getSelfCode())));
        }
        return JsonResponseEntity.success(dto);
    }

    @Override
    public JsonResponseEntity<List<MODto>> selectByCodes(@RequestBody List<String> codes) {
        return JsonResponseEntity.success(MO.covert(service.selectByCodes(codes)));
    }

    @Override
    public JsonResponseEntity<Boolean> addBatch(@RequestBody List<MODto> dtos) {
        service.addBatch(dtos);
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<Boolean> updateSurplus(@RequestParam String selfCode,
                                                     @RequestParam Integer surplus) {
        service.updateSurplus(selfCode, surplus);
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<Boolean> updateStatus(@RequestBody List<String> moCodes,
                                                    @RequestParam MOStatusEnum status) {
        service.updateStatus(moCodes, status);
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<Boolean> updateExecTime(@RequestParam String selfCode,
                                                      @RequestParam Date execTime) {
        service.updateExecTime(selfCode, execTime);
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<Boolean> updateLineCode(@RequestParam String selfCode,
                                                      @RequestParam String lineCode) {
        service.updateLineCode(selfCode, lineCode);
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<Boolean> updateNumAndSurplus(@RequestParam String selfCode,
                                                           @RequestParam Integer num,
                                                           @RequestParam Integer surplus) {
        service.updateNumAndSurplus(selfCode, num, surplus);
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<MoAdjustLogDto> saveAdjustLog(@RequestBody MoAdjustLogDto dto) {
        MoAdjustLog log = MoAdjustLog.wrap(dto);
        if (StringUtils.isBlank(log.getId())) {
            log.setId(IdWorker.getIdStr());
        }
        if (log.getCreateTime() == null) {
            log.setCreateTime(new Date());
        }
        if (log.getStatus() == null) {
            log.setStatus(MoAdjustLogStatusEnum.PENDING.getCode());
        }
        adjustLogService.save(log);
        return JsonResponseEntity.success(MoAdjustLog.covert(log));
    }

    @Override
    public JsonResponseEntity<MoAdjustLogDto> updateAdjustLog(@RequestBody MoAdjustLogDto dto) {
        MoAdjustLog log = MoAdjustLog.wrap(dto);
        adjustLogService.updateById(log);
        return JsonResponseEntity.success(MoAdjustLog.covert(adjustLogService.getById(log.getId())));
    }

    @Override
    public JsonResponseEntity<MoAdjustLogDto> selectAdjustLogByRequestId(@RequestParam String requestId) {
        return JsonResponseEntity.success(MoAdjustLog.covert(adjustLogService.selectByRequestId(requestId)));
    }

    @Override
    public JsonResponseEntity<List<MoAdjustLogDto>> selectAdjustLogsByMoCode(@RequestParam String moCode) {
        return JsonResponseEntity.success(MoAdjustLog.covert(adjustLogService.selectByMoCode(moCode)));
    }

}
