/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.device.controller;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.feign.EquipStateSnapshotFeign;
import com.ourexists.omes.device.feign.WorkshopFeign;
import com.ourexists.omes.device.model.EquipStateSnapshotCountDto;
import com.ourexists.omes.device.model.EquipStateSnapshotCountQuery;
import com.ourexists.omes.device.model.EquipStateSnapshotDto;
import com.ourexists.omes.device.model.EquipStateSnapshotPageQuery;
import com.ourexists.omes.device.model.WorkshopTreeNode;
import com.ourexists.omes.ucenter.feign.RoleFeign;
import com.ourexists.omes.ucenter.role.RoleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@Tag(name = "设备运行记录")
@RestController
@RequestMapping("/equipStateSnapshot")
public class EquipStateSnapshotController {

    @Autowired
    private EquipStateSnapshotFeign feign;

    @Autowired
    private WorkshopFeign workshopFeign;

    @Autowired
    private RoleFeign roleFeign;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EquipStateSnapshotDto>> selectByPage(@RequestBody EquipStateSnapshotPageQuery dto) {
        return feign.selectByPage(dto);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }

    @Operation(summary = "通过时间段统计设备", description = "通过时间段统计设备")
    @PostMapping("countNumByTime")
    public JsonResponseEntity<List<EquipStateSnapshotCountDto>> countNumByTime(@RequestBody EquipStateSnapshotCountQuery dto) {
        try {
            if (Boolean.TRUE.equals(dto.getLimitUserWorkshop())) {
                List<RoleDto> roleDtos = RemoteHandleUtils.getDataFormResponse(
                        roleFeign.selectRoleWhichAccHoldOnly(UserContext.getUser().getId())
                );
                List<String> roleIds = roleDtos.stream().map(RoleDto::getId).toList();
                if (CollectionUtil.isBlank(roleIds)) {
                    return JsonResponseEntity.success(Collections.emptyList());
                }
                List<WorkshopTreeNode> workshopTreeNodes =
                        RemoteHandleUtils.getDataFormResponse(workshopFeign.selectAssignTrees(roleIds, false));
                if (CollectionUtil.isBlank(workshopTreeNodes)) {
                    return JsonResponseEntity.success(Collections.emptyList());
                }
                List<String> workshopCodes = workshopTreeNodes.stream().map(WorkshopTreeNode::getSelfCode).toList();
                dto.setWorkshopCodes(workshopCodes);
            }
        } catch (EraCommonException e) {
            throw new RuntimeException(e);
        }
        return feign.countNumByTime(dto);
    }
}
