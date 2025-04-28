/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.portal.ucenter.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.user.UserInfo;
import com.ourexists.mesedge.ucenter.account.*;
import com.ourexists.mesedge.ucenter.feign.AccountFeign;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
@Tag(name = "账户")
@RestController
@RequestMapping("/acc")
public class AccountController {

    @Autowired
    private AccountFeign accountFeign;

    @Operation(summary = "分页查询")
    @PostMapping("/selectByPage")
    public JsonResponseEntity<List<AccVo>> selectByPage(@RequestBody AccPageQuery pageQuery) {
        return accountFeign.selectByPage(pageQuery);
    }

    @Operation(summary = "注册账户")
    @PostMapping("/register")
    public JsonResponseEntity<String> register(@RequestBody @Valid AccRegisterDto accDto) {
        return accountFeign.register(accDto);
    }

    @Operation(summary = "账户批入驻")
    @PostMapping("/accBathSettle")
    public JsonResponseEntity<AccVo> accBathSettle(@RequestBody @Valid AccBathSettleDto accDto) {
        return accountFeign.accBathSettle(accDto);
    }

    @Operation(summary = "修改密码")
    @PostMapping("/changePass")
    public JsonResponseEntity<Boolean> changePass(@RequestBody @Valid AccChangePassDto accChangePassDto) {
        return accountFeign.changePass(accChangePassDto);
    }

    @Operation(summary = "账户修改")
    @PostMapping("/modify")
    public JsonResponseEntity<Boolean> modify(@RequestBody @Valid AccModifyDto accDto) {
        return accountFeign.modify(accDto);
    }

    @Operation(summary = "账户删除(谨慎使用)")
    @PostMapping("/delete")
    public JsonResponseEntity<Boolean> delete(@RequestBody IdsDto idsDto) {
        return accountFeign.delete(idsDto);
    }

    @Operation(summary = "账户冻结")
    @GetMapping("/frozen")
    public JsonResponseEntity<Boolean> frozen(@RequestParam String accId) {
        return accountFeign.frozen(accId);
    }

    @Operation(summary = "失效用户")
    @GetMapping("/invalid")
    public JsonResponseEntity<Boolean> invalid(@RequestParam String accId) {
        return accountFeign.invalid(accId);
    }

    @Operation(summary = "启用用户")
    @GetMapping("/invoke")
    public JsonResponseEntity<Boolean> invoke(@RequestParam String accId) {
        return accountFeign.invoke(accId);
    }

    @Operation(summary = "查询部门内的所有账户")
    @GetMapping("/selectByDepart")
    public JsonResponseEntity<List<AccVo>> selectByDepart(@RequestParam String departId) {
        return accountFeign.selectByDepart(departId);
    }

    @Operation(summary = "当前用户信息")
    @GetMapping("/currentUser")
    public JsonResponseEntity<UserInfo> currentUser() {
        return JsonResponseEntity.success(UserContext.getUser());
    }

    @Operation(summary = "通过id获取当前用户信息")
    @GetMapping("/selectById")
    public JsonResponseEntity<AccVo> selectById(@RequestParam String id) {
        return accountFeign.selectById(id);
    }
}
