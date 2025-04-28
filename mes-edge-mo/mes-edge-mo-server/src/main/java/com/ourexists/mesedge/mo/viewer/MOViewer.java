/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mo.viewer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.id.IdWorker;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.mo.feign.MOFeign;
import com.ourexists.mesedge.mo.model.MODetailDto;
import com.ourexists.mesedge.mo.model.MODto;
import com.ourexists.mesedge.mo.model.query.MOPageQuery;
import com.ourexists.mesedge.mo.pojo.MO;
import com.ourexists.mesedge.mo.pojo.MODetail;
import com.ourexists.mesedge.mo.service.MODetailService;
import com.ourexists.mesedge.mo.service.MOService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
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
            dto.setSelfCode("MO" + IdWorker.get32UUID());
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


}
