/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.viewer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.mat.feign.BOMFeign;
import com.ourexists.mesedge.mat.model.BOMDDto;
import com.ourexists.mesedge.mat.model.BOMDto;
import com.ourexists.mesedge.mat.model.query.BOMPageQuery;
import com.ourexists.mesedge.mat.pojo.BOM;
import com.ourexists.mesedge.mat.pojo.BOMD;
import com.ourexists.mesedge.mat.service.BOMDService;
import com.ourexists.mesedge.mat.service.BOMService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//@Tag(name = "配方管理")
//@RestController
//@RequestMapping("/BOM")
@Component
public class BOMViewer implements BOMFeign {

    @Autowired
    private BOMService service;

    @Autowired
    private BOMDService detailService;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<BOMDto>> selectByPage(@RequestBody BOMPageQuery dto) {
        Page<BOM> page = service.selectByPage(dto);
        List<BOMDto> r = BOM.covert(page.getRecords());
        if (dto.getQueryDetail()) {
            List<String> selfCodes = r.stream().map(BOMDto::getSelfCode).collect(Collectors.toList());
            List<BOMD> details = detailService.selectByMCode(selfCodes);
            for (BOMDto bomDto : r) {
                List<BOMDDto> dtos = new ArrayList<>();
                for (BOMD detail : details) {
                    if (bomDto.getSelfCode().equals(detail.getMcode())) {
                        dtos.add(BOMD.covert(detail));
                    }
                }
                bomDto.setDetails(dtos);
            }
        }
        return JsonResponseEntity.success(r, OrmUtils.extraPagination(page));
    }

    @Operation(summary = "通过id查询所有", description = "")
    @GetMapping("selectById")
    public JsonResponseEntity<BOMDto> selectById(@RequestParam String id) {
        BOM bom = service.getById(id);
        BOMDto dto = null;
        if (bom != null) {
            dto = BOM.covert(bom);
            List<BOMD> details =
                    detailService.list(new LambdaQueryWrapper<BOMD>().eq(BOMD::getMcode, dto.getSelfCode()).orderByAsc(BOMD::getId));
            dto.setDetails(BOMD.covert(details));
        }
        return JsonResponseEntity.success(dto);
    }


    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody BOMDto dto) {
        service.addOrUpdate(dto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }
}
