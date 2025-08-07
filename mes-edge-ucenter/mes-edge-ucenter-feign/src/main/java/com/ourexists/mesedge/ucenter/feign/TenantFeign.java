/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.account.AccVo;
import com.ourexists.mesedge.ucenter.tenant.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
//@RequestMapping("/tenant")
public interface TenantFeign {


    //    @Operation(summary = "分页查询")
//    @PostMapping("/selectByPage")
    JsonResponseEntity<List<TenantVo>> selectByPage(@RequestBody TenantPageQuery tenantPageQuery);

    @Operation(summary = "查询租户信息")
    @GetMapping("/selectList")
    JsonResponseEntity<List<TenantVo>> selectList(@RequestParam List<String> tenantIds);

    @Operation(summary = "查询租户树")
    @GetMapping("/selectTree")
    JsonResponseEntity<List<TenantTreeNode>> selectTree();

    @Operation(summary = "查询特定节点的租户树")
    @GetMapping("/selectParticularTree")
    JsonResponseEntity<TenantTreeNode> selectParticularTree(@RequestParam String nodeCode);

    @Operation(summary = "租户入驻")
    @PostMapping("/settled")
    JsonResponseEntity<Boolean> settled(@RequestBody @Valid TenantSettledDto tenantSettledDto);

    @Operation(summary = "租户信息修改")
    @PostMapping("/modify")
    JsonResponseEntity<Boolean> modify(@RequestBody @Valid TenantModifyDto tenantModifyDto);

    @Operation(summary = "租户冻结")
    @GetMapping("/frozen")
    JsonResponseEntity<Boolean> frozen(@RequestParam String tenantId);

    @Operation(summary = "租户解冻")
    @GetMapping("/unfrozen")
    JsonResponseEntity<Boolean> unfrozen(@RequestParam String tenantId);

    @Operation(summary = "租户删除")
    @PostMapping("/delete")
    JsonResponseEntity<Boolean> delete(@RequestBody IdsDto idsDto);

    @Operation(summary = "登录用户归属的租户")
    @GetMapping("/tenantToWhichTheCurrentUserBelong")
    JsonResponseEntity<List<TenantNodeVo>> tenantToWhichTheCurrentUserBelong();

    @Operation(summary = "用户归属的租户")
    @GetMapping("/tenantToWhichTheUserBelong")
    JsonResponseEntity<List<TenantVo>> tenantToWhichTheUserBelong(@RequestParam String userId);

    @Operation(summary = "用户归属的可用租户")
    @GetMapping("/availableTenantToWhichTheUserBelong")
    JsonResponseEntity<List<TenantNodeVo>> availableTenantToWhichTheUserBelong(@RequestParam String userId);

    @Operation(summary = "用户归属的可用租户")
    @GetMapping("/availableTenantToWhichTheUsersBelong")
    JsonResponseEntity<List<TenantUVo>> availableTenantToWhichTheUsersBelong(@RequestParam List<String> userIds);

    @Operation(summary = "挂载账户")
    @PostMapping("/mountAcc")
    JsonResponseEntity<Boolean> mountAcc(@Valid @RequestBody TenantMountDto tenantMountDto);

    @Operation(summary = "解除挂载账户")
    @PostMapping("/relieveMountAcc")
    JsonResponseEntity<Boolean> relieveMountAcc(@Valid @RequestBody TenantMountDto tenantMountDto);

    @Operation(summary = "查询当前租户可管控的所有租户")
    @GetMapping("/availableControllableTenantToTheCurrentBelong")
    JsonResponseEntity<List<TenantVo>> availableControllableTenantToTheCurrentBelong();

    @Operation(summary = "根据tenantId或者tenantCode（非主键id）批量查询租户信息")
    @GetMapping("/selectListByTenantCode")
    JsonResponseEntity<List<TenantVo>> selectListByTenantCode(@RequestParam("tenantIds") List<String> tenantIds);


    @Operation(summary = "通过手机号挂载")
    @PostMapping("/mountByPhones")
    JsonResponseEntity<List<AccVo>> mountByPhones(@Valid @RequestBody TenantSettlePhones tenantSettlePhones);

    @Operation(summary = "通过身份证号挂载")
    @PostMapping("/mountByIdCards")
    JsonResponseEntity<List<AccVo>> mountByIdCards(@Valid @RequestBody TenantSettleIdCards tenantSettleIdCards);

    @Operation(summary = "所有租户")
    @GetMapping("/all")
    JsonResponseEntity<List<TenantVo>> all();
}
