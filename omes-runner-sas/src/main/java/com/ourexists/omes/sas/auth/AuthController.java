/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.sas.auth;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.omes.sas.remote.RemoteAccountAuthClient;
import com.ourexists.omes.ucenter.account.AccChangePassDto;
import com.ourexists.omes.ucenter.account.AccChannelRegisterDto;
import com.ourexists.omes.ucenter.account.AccRegisterDto;
import com.ourexists.omes.ucenter.account.AccVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author pengcheng
 * @date 2022/4/19 14:56
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "认证相关")
@RestController
@RequestMapping("/authentication")
public class AuthController {

    @Autowired
    private RemoteAccountAuthClient accountAuthClient;

    @Operation(summary = "通过用户名查询（密码未脱敏）")
    @GetMapping("/selectByAccname")
    public JsonResponseEntity<AccVo> selectByAccname(@RequestParam String accname) {
        return accountAuthClient.selectByAccname(accname, UserContext.getPlatForm());
    }


    @Operation(summary = "通过手机号查询（密码未脱敏）")
    @GetMapping("/selectByMobile")
    public JsonResponseEntity<AccVo> selectByMobile(@RequestParam String mobile) {
        return accountAuthClient.selectByMobile(mobile, UserContext.getPlatForm());
    }

    @Operation(summary = "账户注册")
    @PostMapping("/register")
    public JsonResponseEntity<String> register(@RequestBody @Valid AccRegisterDto accDto) {
        return accountAuthClient.register(accDto);
    }

    @Operation(summary = "渠道注册(无密码注册)")
    @PostMapping("/channelRegister")
    public JsonResponseEntity<String> channelRegister(@RequestBody @Valid AccChannelRegisterDto accDto) {
        return accountAuthClient.channelRegister(accDto);
    }

    @Operation(summary = "渠道注册(无密码注册)")
    @PostMapping("/channelRegisterAndReturn")
    public JsonResponseEntity<AccVo> channelRegisterAndReturn(@RequestBody @Valid AccChannelRegisterDto accDto) {
        return accountAuthClient.channelRegisterAndReturn(accDto);
    }

    @Operation(summary = "修改密码")
    @PostMapping("/changePass")
    public JsonResponseEntity<Boolean> changePass(@RequestBody @Valid AccChangePassDto accChangePassDto) {
        if (StringUtils.isEmpty(accChangePassDto.getPlatform())) {
            accChangePassDto.setPlatform(UserContext.getPlatForm());
        }
        return accountAuthClient.changePass(accChangePassDto);
    }
}
