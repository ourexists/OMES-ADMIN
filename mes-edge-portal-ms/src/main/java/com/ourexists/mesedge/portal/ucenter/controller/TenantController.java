/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.ucenter.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.account.AccVo;
import com.ourexists.mesedge.ucenter.feign.TenantFeign;
import com.ourexists.mesedge.ucenter.tenant.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "租户")
@RestController
@RequestMapping("/tenant")
public class TenantController {

    @Autowired
    private TenantFeign tenantFeign;

    @Operation(summary = "分页查询")
    @PostMapping("/selectByPage")
    public JsonResponseEntity<List<TenantVo>> selectByPage(@RequestBody TenantPageQuery tenantPageQuery) {
        return tenantFeign.selectByPage(tenantPageQuery);
    }

    @Operation(summary = "查询租户信息")
    @GetMapping("/selectList")
    public JsonResponseEntity<List<TenantVo>> selectList(@RequestParam List<String> tenantIds) {
        return tenantFeign.selectList(tenantIds);
    }

    @Operation(summary = "查询租户树")
    @GetMapping("/selectTree")
    public JsonResponseEntity<List<TenantTreeNode>> selectTree() {
        return tenantFeign.selectTree();
    }

    @Operation(summary = "查询特定节点的租户树")
    @GetMapping("/selectParticularTree")
    public JsonResponseEntity<TenantTreeNode> selectParticularTree(@RequestParam String nodeCode) {
        return tenantFeign.selectParticularTree(nodeCode);
    }

    @Operation(summary = "租户入驻")
    @PostMapping("/settled")
    public JsonResponseEntity<Boolean> settled(@RequestBody @Valid TenantSettledDto tenantSettledDto) {
        return tenantFeign.settled(tenantSettledDto);
    }

    @Operation(summary = "租户信息修改")
    @PostMapping("/modify")
    public JsonResponseEntity<Boolean> modify(@RequestBody @Valid TenantModifyDto tenantModifyDto) {
        return tenantFeign.modify(tenantModifyDto);
    }

    @Operation(summary = "租户冻结")
    @GetMapping("/frozen")
    public JsonResponseEntity<Boolean> frozen(@RequestParam String tenantId) {
        return tenantFeign.frozen(tenantId);
    }

    @Operation(summary = "租户解冻")
    @GetMapping("/unfrozen")
    public JsonResponseEntity<Boolean> unfrozen(@RequestParam String tenantId) {
        return tenantFeign.unfrozen(tenantId);
    }

    @Operation(summary = "租户删除")
    @PostMapping("/delete")
    public JsonResponseEntity<Boolean> delete(@RequestBody IdsDto idsDto) {
        return tenantFeign.delete(idsDto);
    }

    @Operation(summary = "登录用户归属的租户")
    @GetMapping("/tenantToWhichTheCurrentUserBelong")
    public JsonResponseEntity<List<TenantNodeVo>> tenantToWhichTheCurrentUserBelong() {
        return tenantFeign.tenantToWhichTheCurrentUserBelong();
    }

    @Operation(summary = "用户归属的租户")
    @GetMapping("/tenantToWhichTheUserBelong")
    public JsonResponseEntity<List<TenantVo>> tenantToWhichTheUserBelong(@RequestParam String userId) {
        return tenantFeign.tenantToWhichTheUserBelong(userId);
    }

    @Operation(summary = "用户归属的可用租户")
    @GetMapping("/availableTenantToWhichTheUserBelong")
    public JsonResponseEntity<List<TenantNodeVo>> availableTenantToWhichTheUserBelong(@RequestParam String userId) {
        return tenantFeign.availableTenantToWhichTheUserBelong(userId);
    }

    @Operation(summary = "用户归属的可用租户")
    @GetMapping("/availableTenantToWhichTheUsersBelong")
    public JsonResponseEntity<List<TenantUVo>> availableTenantToWhichTheUsersBelong(@RequestParam List<String> userIds) {
        return tenantFeign.availableTenantToWhichTheUsersBelong(userIds);
    }

    @Operation(summary = "挂载账户")
    @PostMapping("/mountAcc")
    public JsonResponseEntity<Boolean> mountAcc(@Valid @RequestBody TenantMountDto tenantMountDto) {
        return tenantFeign.mountAcc(tenantMountDto);
    }

    @Operation(summary = "解除挂载账户")
    @PostMapping("/relieveMountAcc")
    public JsonResponseEntity<Boolean> relieveMountAcc(@Valid @RequestBody TenantMountDto tenantMountDto) {
        return tenantFeign.relieveMountAcc(tenantMountDto);
    }

    @Operation(summary = "查询当前租户可管控的所有租户")
    @GetMapping("/availableControllableTenantToTheCurrentBelong")
    public JsonResponseEntity<List<TenantVo>> availableControllableTenantToTheCurrentBelong() {
        return tenantFeign.availableControllableTenantToTheCurrentBelong();
    }

    @Operation(summary = "根据tenantId或者tenantCode（非主键id）批量查询租户信息")
    @GetMapping("/selectListByTenantCode")
    public JsonResponseEntity<List<TenantVo>> selectListByTenantCode(@RequestParam("tenantIds") List<String> tenantIds) {
        return tenantFeign.selectListByTenantCode(tenantIds);
    }


    @Operation(summary = "通过手机号挂载")
    @PostMapping("/mountByPhones")
    public JsonResponseEntity<List<AccVo>> mountByPhones(@Valid @RequestBody TenantSettlePhones tenantSettlePhones) {
        return tenantFeign.mountByPhones(tenantSettlePhones);
    }

    @Operation(summary = "通过身份证号挂载")
    @PostMapping("/mountByIdCards")
    public JsonResponseEntity<List<AccVo>> mountByIdCards(@Valid @RequestBody TenantSettleIdCards tenantSettleIdCards) {
        return tenantFeign.mountByIdCards(tenantSettleIdCards);
    }
}
