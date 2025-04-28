/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.viewer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.tree.TreeUtil;
import com.ourexists.mesedge.mat.feign.BOMCFeign;
import com.ourexists.mesedge.mat.model.BOMCDto;
import com.ourexists.mesedge.mat.model.BOMTreeNode;
import com.ourexists.mesedge.mat.pojo.BOMC;
import com.ourexists.mesedge.mat.service.BOMCService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.stream.Collectors;

//@Tag(name = "配方分类")
//@RestController
//@RequestMapping("/BOMC")
@Component
public class BOMCViewer implements BOMCFeign {

    @Autowired
    private BOMCService service;

    @Operation(summary = "分类树", description = "分类树")
    @GetMapping("treeClassify")
    public JsonResponseEntity<List<BOMTreeNode>> treeClassify() {
        List<BOMTreeNode> nodes = BOMC.covert(service.list());
        nodes = TreeUtil.foldRootTree(nodes);
        return JsonResponseEntity.success(nodes);
    }

    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody BOMCDto dto) {
        if (StringUtils.isBlank(dto.getPcode())) {
            dto.setPcode(TreeUtil.ROOT_CODE);
        }
        //生成级联编号
        if (StringUtils.isBlank(dto.getCode())) {
            BOMC bomc = service.getOne(new LambdaQueryWrapper<BOMC>()
                    .eq(BOMC::getPcode, dto.getPcode())
                    .orderByDesc(BOMC::getCode)
                    .last("limit 1")
            );
            String otherMaxCode = bomc == null ? null : bomc.getCode();
            dto.setCode(TreeUtil.generateCode(dto.getPcode(), otherMaxCode));
        }
        service.saveOrUpdate(BOMC.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        List<BOMC> BOMCList = service.listByIds(idsDto.getIds());
        if (CollectionUtil.isBlank(BOMCList)) {
            return JsonResponseEntity.success(true);
        }
        List<String> codes = BOMCList.stream().map(BOMC::getCode).collect(Collectors.toList());
        List<BOMC> children = service.list(new LambdaQueryWrapper<BOMC>()
                .in(BOMC::getPcode, codes));
        if (CollectionUtil.isNotBlank(children)) {
            StringBuilder msg = new StringBuilder();
            for (BOMC child : children) {
                msg.append(child.getName()).append(",");
            }
            throw new BusinessException("{common.msg.delete.existchild}", msg.substring(0, msg.length() - 1));
        }
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }
}
