/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.portal.ucenter.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.feign.RoleFeign;
import com.ourexists.mesedge.ucenter.role.AccRoleBindDto;
import com.ourexists.mesedge.ucenter.role.DepartRoleBindDto;
import com.ourexists.mesedge.ucenter.role.RoleDto;
import com.ourexists.mesedge.ucenter.role.RolePageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "角色")
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleFeign roleFeign;


    @Operation(summary = "分页查询")
    @PostMapping("/selectByPage")
    public JsonResponseEntity<List<RoleDto>> selectByPage(@RequestBody RolePageQuery pageQuery) {
        return roleFeign.selectByPage(pageQuery);
    }

    @Operation(summary = "新增更新角色")
    @PostMapping("/addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@RequestBody @Validated RoleDto dto) {
        return roleFeign.addOrUpdate(dto);
    }

    @Operation(summary = "删除角色")
    @PostMapping("/delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return roleFeign.delete(idsDto);
    }

    @Operation(summary = "用户持有角色(仅账户)")
    @GetMapping("/selectRoleWhichAccHoldOnly")
    public JsonResponseEntity<List<RoleDto>> selectRoleWhichAccHoldOnly(@RequestParam String accId) {
        return roleFeign.selectRoleWhichAccHoldOnly(accId);
    }

    @Operation(summary = "用户持有角色(全系统)")
    @GetMapping("/selectRoleWhichAccHold")
    public JsonResponseEntity<List<RoleDto>> selectRoleWhichAccHold(@RequestParam String accId) {
        return roleFeign.selectRoleWhichAccHold(accId);
    }

    @Operation(summary = "部门持有角色")
    @GetMapping("/selectRoleWhichDepartHold")
    public JsonResponseEntity<List<RoleDto>> selectRoleWhichDepartHold(@RequestParam String departId) {
        return roleFeign.selectRoleWhichDepartHold(departId);
    }

    @Operation(summary = "绑定部门角色")
    @PostMapping("/bindDepart")
    public JsonResponseEntity<Boolean> bindDepart(@Validated @RequestBody DepartRoleBindDto dto) {
        return roleFeign.bindDepart(dto);
    }

    @Operation(summary = "绑定账户角色")
    @PostMapping("/bindAcc")
    public JsonResponseEntity<Boolean> bindAcc(@Validated @RequestBody AccRoleBindDto dto) {
        return roleFeign.bindAcc(dto);
    }
}
