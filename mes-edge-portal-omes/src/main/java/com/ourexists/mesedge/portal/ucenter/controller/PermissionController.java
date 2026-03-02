/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.ucenter.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.enums.PermissionStrategyEnum;
import com.ourexists.mesedge.ucenter.enums.PermissionTypeEnum;
import com.ourexists.mesedge.ucenter.feign.PermissionFeign;
import com.ourexists.mesedge.ucenter.permission.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "权限")
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private PermissionFeign permissionFeign;

    @Operation(summary = "分配给租户权限树")
    @PostMapping("/assignToTenantPermissionTree")
    public JsonResponseEntity<Boolean> assignToTenantPermissionTree(@RequestBody @Validated PermissionAssignDto assignDto) {
        return permissionFeign.assignToTenantPermissionTree(assignDto);
    }

    @Operation(summary = "分配给角色权限树")
    @PostMapping("/assignToRolePermissionTree")
    public JsonResponseEntity<Boolean> assignToRolePermissionTree(@RequestBody @Validated PermissionAssignDto assignDto) {
        return permissionFeign.assignToRolePermissionTree(assignDto);
    }

    @Operation(summary = "分配api给权限")
    @PostMapping("/assignApiToPermission")
    public JsonResponseEntity<Boolean> assignApiToPermission(@RequestBody @Validated PermissionApiDto assignDto) {
        return permissionFeign.assignApiToPermission(assignDto);
    }

    @Operation(summary = "新增权限")
    @PostMapping("/add")
    public JsonResponseEntity<Boolean> add(@RequestBody @Validated PermissionDto dto) {
        return permissionFeign.add(dto);
    }

    @Operation(summary = "更新权限")
    @PostMapping("/modify")
    public JsonResponseEntity<Boolean> modify(@RequestBody @Validated PermissionModifyDto modifyDto) {
        return permissionFeign.modify(modifyDto);
    }

    @Operation(summary = "删除权限")
    @GetMapping("/delete")
    public JsonResponseEntity<Boolean> delete(@RequestParam String id) {
        return permissionFeign.delete(id);
    }

    @Operation(summary = "所有管理的权限树")
    @GetMapping("/allPermissionTree")
    public JsonResponseEntity<List<PermissionTreeNode>> currentPlatformPermissionTree() {
        return permissionFeign.currentPlatformPermissionTree();
    }

    @Operation(summary = "系统当前用户的权限树")
    @GetMapping("/currentAccPermissionTree")
    public JsonResponseEntity<List<PermissionTreeNode>> currentAccPermissionTree() {
        return permissionFeign.currentAccPermissionTree();
    }

    @Operation(summary = "当前租户的权限树")
    @GetMapping("/currentTenantPermissionTree")
    public JsonResponseEntity<List<PermissionTreeNode>> currentTenantPermissionTree() {
        return permissionFeign.currentTenantPermissionTree();
    }

    @Operation(summary = "某平台下租户的权限树")
    @GetMapping("/selectTenantPermissionTreeInPlatform")
    public JsonResponseEntity<List<PermissionTreeNode>> selectTenantPermissionTreeInPlatform(@RequestParam String tenantId,
                                                                                             @RequestParam String platform) {
        return permissionFeign.selectTenantPermissionTreeInPlatform(tenantId, platform);
    }

    @Operation(summary = "查询对应平台下角色的权限树")
    @GetMapping("/selectPermissionWhichRoleHold")
    public JsonResponseEntity<List<PermissionTreeNode>> selectPermissionWhichRoleHold(@RequestParam String roleId,
                                                                                      @RequestParam String platform) {
        return permissionFeign.selectPermissionWhichRoleHold(roleId, platform);
    }

    @Operation(summary = "查询租户的全平台权限树")
    @GetMapping("/selectTenantPermissionTree")
    public JsonResponseEntity<List<PermissionTreeNode>> selectTenantPermissionTree(@RequestParam String tenant) {
        return permissionFeign.selectTenantPermissionTree(tenant);
    }

    @Operation(summary = "查询平台权限树")
    @GetMapping("/selectPermissionTreeInPlatform")
    public JsonResponseEntity<List<PermissionTreeNode>> selectPermissionTreeInPlatform(@RequestParam String platform) {
        return permissionFeign.selectPermissionTreeInPlatform(platform);
    }

    @Operation(summary = "查询角色权限")
    @GetMapping("/selectRolePermission")
    public JsonResponseEntity<List<PermissionDto>> selectRolePermission(@RequestParam String roleId) {
        return permissionFeign.selectRolePermission(roleId);
    }

    @Operation(summary = "查询角色权限树")
    @GetMapping("/selectRolePermissionTree")
    public JsonResponseEntity<List<PermissionTreeNode>> selectRolePermissionTree(@RequestParam String roleId) {
        return permissionFeign.selectRolePermissionTree(roleId);
    }

    @Operation(summary = "查询接口权限")
    @GetMapping("/selectPermissionApi")
    public JsonResponseEntity<List<PermissionApiDetailDto>> selectPermissionApi(@RequestParam String permissionId) {
        return permissionFeign.selectPermissionApi(permissionId);
    }

    @Operation(summary = "查询接口权限")
    @GetMapping("/selectAll")
    public JsonResponseEntity<List<PermissionApiDetailDto>> selectAll() {
        return permissionFeign.selectAll();
    }

    @Operation(summary = "所有权限类型")
    @GetMapping("/permissionType")
    public JsonResponseEntity<Map<Integer, String>> permissionType() {
        Map<Integer, String> result = new HashMap<>();
        for (PermissionTypeEnum value : PermissionTypeEnum.values()) {
            result.put(value.getCode(), value.name());
        }
        return JsonResponseEntity.success(result);
    }

    @Operation(summary = "所有权限策略")
    @GetMapping("/permissionStrategy")
    public JsonResponseEntity<Map<Integer, String>> permissionStrategy() {
        Map<Integer, String> result = new HashMap<>();
        for (PermissionStrategyEnum value : PermissionStrategyEnum.values()) {
            result.put(value.getCode(), value.name());
        }
        return JsonResponseEntity.success(result);
    }
}
