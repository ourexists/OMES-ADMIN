/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.line.viewer;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.line.feign.LineFeign;
import com.ourexists.mesedge.line.model.LineDto;
import com.ourexists.mesedge.line.model.LineVo;
import com.ourexists.mesedge.line.model.ResetLineTFDto;
import com.ourexists.mesedge.line.model.query.LinePageQuery;
import com.ourexists.mesedge.line.pojo.Line;
import com.ourexists.mesedge.line.pojo.TF;
import com.ourexists.mesedge.line.service.LineService;
import com.ourexists.mesedge.line.service.TFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

@Component
public class LineViewer implements LineFeign {

    @Autowired
    private LineService service;

    @Autowired
    private TFService tfService;

    //    @Operation(summary = "分页", description = "")
//    @PostMapping("selectByPage")
    public JsonResponseEntity<List<LineVo>> selectByPage(@RequestBody LinePageQuery dto) {
        Page<Line> page = service.selectByPage(dto);
        return JsonResponseEntity.success(Line.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    //    @Operation(summary = "通过code查询所有", description = "")
//    @GetMapping("selectByCode")
    public JsonResponseEntity<LineVo> selectByCode(@RequestParam String code) {
        LineVo line = Line.covert(service.selectByCode(code));
        if (line != null) {
            line.setTfs(TF.covert(tfService.selectByLineId(line.getId())));
        }
        return JsonResponseEntity.success(line);
    }


    //    @Operation(summary = "通过id查询所有", description = "")
//    @GetMapping("selectById")
    public JsonResponseEntity<LineVo> selectById(@RequestParam String id) {
        LineVo line = Line.covert(service.getById(id));
        if (line != null) {
            line.setTfs(TF.covert(tfService.selectByLineId(line.getId())));
        }
        return JsonResponseEntity.success(line);
    }

    //    @Operation(summary = "新增或修改根据id", description = "")
//    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody LineDto dto) {
        Line line = service.getOne(new LambdaQueryWrapper<Line>().eq(Line::getSelfCode, dto.getSelfCode()));
        if (line != null && !line.getId().equals(dto.getId())) {
            throw new BusinessException("${valid.code.duplicate}");
        }
        service.saveOrUpdate(Line.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    //    @Operation(summary = "删除", description = "")
//    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    //    @Operation(summary = "重置工艺", description = "")
//    @PostMapping("resetLineTF")
    public JsonResponseEntity<Boolean> resetLineTF(@RequestBody ResetLineTFDto dto) {
        service.resetLineTF(dto);
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<Boolean> updatePushTime(@RequestParam String lineId) {
        service.update(new LambdaUpdateWrapper<Line>()
                .set(Line::getPlcTime, new Date())
                .eq(Line::getId, lineId));
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<List<LineVo>> selectByCodes(List<String> codes) {
        return JsonResponseEntity.success(Line.covert(service.selectByCodes(codes)));
    }
}
