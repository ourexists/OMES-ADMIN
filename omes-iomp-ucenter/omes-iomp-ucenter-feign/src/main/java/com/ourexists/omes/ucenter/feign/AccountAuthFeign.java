/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.ucenter.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.ucenter.account.AccChangePassDto;
import com.ourexists.omes.ucenter.account.AccChannelRegisterDto;
import com.ourexists.omes.ucenter.account.AccRegisterDto;
import com.ourexists.omes.ucenter.account.AccVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 认证场景账户数据接口（由 ucenter-server 实现，SAS 等进程通过 Feign 远程调用）。
 */
public interface AccountAuthFeign {

    @GetMapping("/authentication/selectByAccname")
    JsonResponseEntity<AccVo> selectByAccname(@RequestParam String accname,
                                              @RequestParam(required = false) String platform);

    @GetMapping("/authentication/selectByMobile")
    JsonResponseEntity<AccVo> selectByMobile(@RequestParam String mobile,
                                             @RequestParam(required = false) String platform);

    @PostMapping("/authentication/register")
    JsonResponseEntity<String> register(@RequestBody @Valid AccRegisterDto accDto);

    @PostMapping("/authentication/channelRegister")
    JsonResponseEntity<String> channelRegister(@RequestBody @Valid AccChannelRegisterDto accDto);

    @PostMapping("/authentication/channelRegisterAndReturn")
    JsonResponseEntity<AccVo> channelRegisterAndReturn(@RequestBody @Valid AccChannelRegisterDto accDto);

    @PostMapping("/authentication/changePass")
    JsonResponseEntity<Boolean> changePass(@RequestBody @Valid AccChangePassDto accChangePassDto);
}
