/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.ucenter.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.ucenter.account.AccChangePassDto;
import com.ourexists.omes.ucenter.account.AccChannelRegisterDto;
import com.ourexists.omes.ucenter.account.AccRegisterDto;
import com.ourexists.omes.ucenter.account.AccVo;
import com.ourexists.omes.ucenter.feign.AccountAuthFeign;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证场景账户 API（数据层在 ucenter-server，供 SAS 等通过 Feign 远程调用）。
 */
@Tag(name = "认证账户")
@RestController
@RequestMapping("/authentication")
public class AccountAuthController {

    @Autowired
    private AccountAuthFeign accountAuthFeign;

    @Operation(summary = "通过用户名查询（密码未脱敏）")
    @GetMapping("/selectByAccname")
    public JsonResponseEntity<AccVo> selectByAccname(@RequestParam String accname,
                                                     @RequestParam(required = false) String platform) {
        return accountAuthFeign.selectByAccname(accname, platform);
    }

    @Operation(summary = "通过手机号查询（密码未脱敏）")
    @GetMapping("/selectByMobile")
    public JsonResponseEntity<AccVo> selectByMobile(@RequestParam String mobile,
                                                    @RequestParam(required = false) String platform) {
        return accountAuthFeign.selectByMobile(mobile, platform);
    }

    @Operation(summary = "账户注册")
    @PostMapping("/register")
    public JsonResponseEntity<String> register(@RequestBody @Valid AccRegisterDto accDto) {
        return accountAuthFeign.register(accDto);
    }

    @Operation(summary = "渠道注册(无密码注册)")
    @PostMapping("/channelRegister")
    public JsonResponseEntity<String> channelRegister(@RequestBody @Valid AccChannelRegisterDto accDto) {
        return accountAuthFeign.channelRegister(accDto);
    }

    @Operation(summary = "渠道注册并返回账户")
    @PostMapping("/channelRegisterAndReturn")
    public JsonResponseEntity<AccVo> channelRegisterAndReturn(@RequestBody @Valid AccChannelRegisterDto accDto) {
        return accountAuthFeign.channelRegisterAndReturn(accDto);
    }

    @Operation(summary = "修改密码")
    @PostMapping("/changePass")
    public JsonResponseEntity<Boolean> changePass(@RequestBody @Valid AccChangePassDto accChangePassDto) {
        return accountAuthFeign.changePass(accChangePassDto);
    }
}
