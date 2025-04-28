/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.permission.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
//@RequestMapping("/permission")
public interface PermissionFeign {

//    @Operation(summary = "分配给租户权限树")
//    @PostMapping("/assignToTenantPermissionTree")
    JsonResponseEntity<Boolean> assignToTenantPermissionTree(@RequestBody @Validated PermissionAssignDto assignDto);

//    @Operation(summary = "分配给角色权限树")
//    @PostMapping("/assignToRolePermissionTree")
    JsonResponseEntity<Boolean> assignToRolePermissionTree(@RequestBody @Validated PermissionAssignDto assignDto);

//    @Operation(summary = "分配api给权限")
//    @PostMapping("/assignApiToPermission")
    JsonResponseEntity<Boolean> assignApiToPermission(@RequestBody @Validated PermissionApiDto assignDto);

//    @Operation(summary = "新增权限")
//    @PostMapping("/add")
    JsonResponseEntity<Boolean> add(@RequestBody @Validated PermissionDto dto);

//    @Operation(summary = "更新权限")
//    @PostMapping("/modify")
    JsonResponseEntity<Boolean> modify(@RequestBody @Validated PermissionModifyDto modifyDto);

//    @Operation(summary = "删除权限")
//    @GetMapping("/delete")
    JsonResponseEntity<Boolean> delete(@RequestParam String id);

//    @Operation(summary = "所有管理的权限树")
//    @GetMapping("/allPermissionTree")
    JsonResponseEntity<List<PermissionTreeNode>> currentPlatformPermissionTree();

//    @Operation(summary = "系统当前用户的权限树")
//    @GetMapping("/currentAccPermissionTree")
    JsonResponseEntity<List<PermissionTreeNode>> currentAccPermissionTree();

//    @Operation(summary = "当前租户的权限树")
//    @GetMapping("/currentTenantPermissionTree")
    JsonResponseEntity<List<PermissionTreeNode>> currentTenantPermissionTree();

//    @Operation(summary = "某平台下租户的权限树")
//    @GetMapping("/selectTenantPermissionTreeInPlatform")
    JsonResponseEntity<List<PermissionTreeNode>> selectTenantPermissionTreeInPlatform(@RequestParam String tenantId,
                                                                                      @RequestParam String platform);

//    @Operation(summary = "查询对应平台下角色的权限树")
//    @GetMapping("/selectPermissionWhichRoleHold")
    JsonResponseEntity<List<PermissionTreeNode>> selectPermissionWhichRoleHold(@RequestParam String roleId,
                                                                           @RequestParam String platform);

//    @Operation(summary = "查询租户的全平台权限树")
//    @GetMapping("/selectTenantPermissionTree")
    JsonResponseEntity<List<PermissionTreeNode>> selectTenantPermissionTree(@RequestParam String tenant);

//    @Operation(summary = "查询平台权限树")
//    @GetMapping("/selectPermissionTreeInPlatform")
    JsonResponseEntity<List<PermissionTreeNode>> selectPermissionTreeInPlatform(@RequestParam String platform);

//    @Operation(summary = "查询角色权限")
//    @GetMapping("/selectRolePermission")
    JsonResponseEntity<List<PermissionDto>> selectRolePermission(@RequestParam String roleId);

//    @Operation(summary = "查询角色权限树")
//    @GetMapping("/selectRolePermissionTree")
    JsonResponseEntity<List<PermissionTreeNode>> selectRolePermissionTree(@RequestParam String roleId);

//    @Operation(summary = "查询接口权限")
//    @GetMapping("/selectPermissionApi")
    JsonResponseEntity<List<PermissionApiDetailDto>> selectPermissionApi(@RequestParam String permissionId);

//    @Operation(summary = "查询接口权限")
//    @GetMapping("/selectAll")
    JsonResponseEntity<List<PermissionApiDetailDto>> selectAll();
}
