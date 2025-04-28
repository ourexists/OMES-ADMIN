/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.viewer;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.tree.TreeUtil;
import com.ourexists.era.framework.oauth2.AccRole;
import com.ourexists.mesedge.ucenter.feign.PermissionFeign;
import com.ourexists.mesedge.ucenter.permission.*;
import com.ourexists.mesedge.ucenter.permission.pojo.Permission;
import com.ourexists.mesedge.ucenter.permission.pojo.PermissionApi;
import com.ourexists.mesedge.ucenter.permission.service.PermissionApiService;
import com.ourexists.mesedge.ucenter.permission.service.PermissionService;
import com.ourexists.mesedge.ucenter.permission.service.RolePermissionService;
import com.ourexists.mesedge.ucenter.permission.service.TenantPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
//@Slf4j
//@Tag(name = "权限")
//@RestController
//@RequestMapping("/permission")
@Component
public class PermissionViewer implements PermissionFeign {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private PermissionApiService permissionApiService;

    @Autowired
    private TenantPermissionService tenantPermissionService;

    @Autowired
    private RolePermissionService rolePermissionService;

//    @Operation(summary = "分配给租户权限树")
//    @PostMapping("/assignToTenantPermissionTree")
    public JsonResponseEntity<Boolean> assignToTenantPermissionTree(@RequestBody @Validated PermissionAssignDto assignDto) {
        tenantPermissionService.establishRelation(assignDto.getId(), assignDto.getPermissionIds());
        return JsonResponseEntity.success(true);
    }

//    @Operation(summary = "分配给角色权限树")
//    @PostMapping("/assignToRolePermissionTree")
    public JsonResponseEntity<Boolean> assignToRolePermissionTree(@RequestBody @Validated PermissionAssignDto assignDto) {
        rolePermissionService.establishRelation(assignDto.getId(), assignDto.getPermissionIds());
        return JsonResponseEntity.success(true);
    }

//    @Operation(summary = "分配api给权限")
//    @PostMapping("/assignApiToPermission")
    public JsonResponseEntity<Boolean> assignApiToPermission(@RequestBody @Validated PermissionApiDto assignDto) {
        permissionApiService.assignApiToPermission(assignDto.getPermissionId(), assignDto.getDetailDtos());
        return JsonResponseEntity.success(true);
    }

//    @Operation(summary = "新增权限")
//    @PostMapping("/add")
    public JsonResponseEntity<Boolean> add(@RequestBody @Validated PermissionDto dto) {
        permissionService.add(dto);
        return JsonResponseEntity.success(true);
    }

//    @Operation(summary = "更新权限")
//    @PostMapping("/modify")
    public JsonResponseEntity<Boolean> modify(@RequestBody @Validated PermissionModifyDto modifyDto) {
        permissionService.modify(modifyDto);
        return JsonResponseEntity.success(true);
    }

//    @Operation(summary = "删除权限")
//    @GetMapping("/delete")
    public JsonResponseEntity<Boolean> delete(@RequestParam String id) {
        permissionService.removeById(id);
        return JsonResponseEntity.success(true);
    }

//    @Operation(summary = "所有管理的权限树")
//    @GetMapping("/allPermissionTree")
    public JsonResponseEntity<List<PermissionTreeNode>> currentPlatformPermissionTree() {
        List<Permission> permissionList = permissionService.selectPermissionWhichPlatformHold(UserContext.getPlatForm());
        return JsonResponseEntity.success(TreeUtil.foldRootTree(Permission.covert(permissionList)));
    }

//    @Operation(summary = "系统当前用户的权限树")
//    @GetMapping("/currentAccPermissionTree")
    public JsonResponseEntity<List<PermissionTreeNode>> currentAccPermissionTree() {
        if (UserContext.getTenant().getRole().equals(AccRole.ADMIN.name())) {
            List<Permission> permissionList = permissionService.selectPermissionWhichTenantHold(
                    UserContext.getTenant().getTenantId(),
                    UserContext.getPlatForm());
            return JsonResponseEntity.success(TreeUtil.foldRootTree(Permission.covert(permissionList)));
        }
        List<PermissionTreeNode> permissionTreeNodes =
                Permission.covert(permissionService.selectPermissionWhichAccHold(
                        UserContext.getUser().getId(), UserContext.getPlatForm()));
        return JsonResponseEntity.success(TreeUtil.foldRootTree(permissionTreeNodes));
    }

//    @Operation(summary = "当前租户的权限树")
//    @GetMapping("/currentTenantPermissionTree")
    public JsonResponseEntity<List<PermissionTreeNode>> currentTenantPermissionTree() {
        List<Permission> permissionList = permissionService.selectPermissionWhichTenantHold(
                UserContext.getTenant().getTenantId(),
                UserContext.getPlatForm());
        return JsonResponseEntity.success(TreeUtil.foldRootTree(Permission.covert(permissionList)));
    }

//    @Operation(summary = "某平台下租户的权限树")
//    @GetMapping("/selectTenantPermissionTreeInPlatform")
    public JsonResponseEntity<List<PermissionTreeNode>> selectTenantPermissionTreeInPlatform(@RequestParam String tenantId,
                                                                                             @RequestParam String platform) {
        List<Permission> permissionList = permissionService.selectPermissionWhichTenantHold(
                tenantId,
                platform);
        return JsonResponseEntity.success(TreeUtil.foldRootTree(Permission.covert(permissionList)));
    }

//    @Operation(summary = "查询对应平台下角色的权限树")
//    @GetMapping("/selectPermissionWhichRoleHold")
    public JsonResponseEntity<List<PermissionTreeNode>> selectPermissionWhichRoleHold(@RequestParam String roleId,
                                                                                      @RequestParam String platform) {
        List<Permission> permissionList = permissionService.selectPermissionWhichRoleHold(roleId, platform);
        return JsonResponseEntity.success(Permission.covert(permissionList));
    }

//    @Operation(summary = "查询租户的全平台权限树")
//    @GetMapping("/selectTenantPermissionTree")
    public JsonResponseEntity<List<PermissionTreeNode>> selectTenantPermissionTree(@RequestParam String tenant) {
        List<Permission> permissionList = permissionService.selectPermissionWhichTenantHold(tenant);
        return JsonResponseEntity.success(TreeUtil.foldRootTree(Permission.covert(permissionList)));
    }

//    @Operation(summary = "查询平台权限树")
//    @GetMapping("/selectPermissionTreeInPlatform")
    public JsonResponseEntity<List<PermissionTreeNode>> selectPermissionTreeInPlatform(@RequestParam String platform) {
        List<Permission> permissionList = permissionService.selectPermissionWhichPlatformHold(platform);
        return JsonResponseEntity.success(TreeUtil.foldRootTree(Permission.covert(permissionList)));
    }

//    @Operation(summary = "查询角色权限")
//    @GetMapping("/selectRolePermission")
    public JsonResponseEntity<List<PermissionDto>> selectRolePermission(@RequestParam String roleId) {
        return JsonResponseEntity.success(Permission.covertDto(permissionService.selectPermissionWhichRoleHold(roleId)));
    }

//    @Operation(summary = "查询角色权限树")
//    @GetMapping("/selectRolePermissionTree")
    public JsonResponseEntity<List<PermissionTreeNode>> selectRolePermissionTree(@RequestParam String roleId) {
        List<PermissionTreeNode> allTreeNodes = Permission.covert(permissionService.selectPermissionWhichRoleHold(roleId));
        return JsonResponseEntity.success(TreeUtil.foldRootTree(allTreeNodes));
    }

//    @Operation(summary = "查询接口权限")
//    @GetMapping("/selectPermissionApi")
    public JsonResponseEntity<List<PermissionApiDetailDto>> selectPermissionApi(@RequestParam String permissionId) {
        return JsonResponseEntity.success(PermissionApi.covert(permissionApiService.selectByPermissionId(permissionId)));
    }

//    @Operation(summary = "查询接口权限")
//    @GetMapping("/selectAll")
    public JsonResponseEntity<List<PermissionApiDetailDto>> selectAll() {
        return JsonResponseEntity.success(PermissionApi.covert(permissionApiService.list()));
    }
}
