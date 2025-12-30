/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.viewer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.tree.TreeUtil;
import com.ourexists.mesedge.device.feign.WorkshopFeign;
import com.ourexists.mesedge.device.model.WorkshopDto;
import com.ourexists.mesedge.device.model.WorkshopTreeNode;
import com.ourexists.mesedge.device.pojo.Workshop;
import com.ourexists.mesedge.device.service.EquipService;
import com.ourexists.mesedge.device.service.WorkshopService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//@Tag(name = "配方管理")
//@RestController
//@RequestMapping("/BOM")
@Component
public class WorkshopViewer implements WorkshopFeign {

    @Autowired
    private WorkshopService service;

    @Operation(summary = "查询所有树", description = "查询所有树")
    @GetMapping("selectTree")
    public JsonResponseEntity<List<WorkshopTreeNode>> selectTree() {
        List<WorkshopTreeNode> nodes = Workshop.covert(service.list());
        nodes = TreeUtil.foldRootTree(nodes);
        return JsonResponseEntity.success(nodes);
    }


    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody WorkshopDto dto) {
        if (StringUtils.isBlank(dto.getPcode())) {
            dto.setPcode(TreeUtil.ROOT_CODE);
        }
        //生成级联编号
        if (StringUtils.isBlank(dto.getCode())) {
            Workshop s = service.getOne(new LambdaQueryWrapper<Workshop>()
                    .eq(Workshop::getPcode, dto.getPcode())
                    .orderByDesc(Workshop::getCode)
                    .last("limit 1"));
            String otherMaxCode = s == null ? null : s.getCode();
            dto.setCode(TreeUtil.generateCode(dto.getPcode(), otherMaxCode));
        }
        service.addOrUpdate(dto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {

        service.delete(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }
}
