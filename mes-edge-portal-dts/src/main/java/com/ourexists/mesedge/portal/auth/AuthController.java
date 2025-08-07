/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.portal.auth;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.mesedge.ucenter.account.AccChangePassDto;
import com.ourexists.mesedge.ucenter.account.AccChannelRegisterDto;
import com.ourexists.mesedge.ucenter.account.AccRegisterDto;
import com.ourexists.mesedge.ucenter.account.AccVo;
import com.ourexists.mesedge.ucenter.account.pojo.Account;
import com.ourexists.mesedge.ucenter.account.service.AccountService;
import com.ourexists.mesedge.ucenter.enums.AccRoleEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
    private AccountService accountService;

    @Operation(summary = "通过用户名查询（密码未脱敏）")
    @GetMapping("/selectByAccname")
    public JsonResponseEntity<AccVo> selectByAccname(@RequestParam String accname) {
        Account account = accountService.selectByAccName(accname, UserContext.getPlatForm());
        if (account == null) {
            return JsonResponseEntity.success(null);
        }
        AccVo accVo = Account.covert(account, false);
        return JsonResponseEntity.success(accountService.extraInfo(accVo));
    }


    @Operation(summary = "通过用户名查询（密码未脱敏）")
    @GetMapping("/selectByMobile")
    public JsonResponseEntity<AccVo> selectByMobile(@RequestParam String mobile) {
        Account account = accountService.selectByMobile(mobile, UserContext.getPlatForm());
        if (account == null) {
            return JsonResponseEntity.success(null);
        }
        return JsonResponseEntity.success(accountService.extraInfo(Account.covert(account, false)));
    }

    @Operation(summary = "账户注册")
    @PostMapping("/register")
    public JsonResponseEntity<String> register(@RequestBody @Valid AccRegisterDto accDto) {
        return JsonResponseEntity.success(accountService.register(Account.warp(accDto), accDto.getTenantId(), AccRoleEnum.valueOf(accDto.getAccRole())));
    }

    @Operation(summary = "渠道注册(无密码注册)")
    @PostMapping("/channelRegister")
    public JsonResponseEntity<String> channelRegister(@RequestBody @Valid AccChannelRegisterDto accDto) {
        Account account = new Account();
        BeanUtils.copyProperties(accDto, account);
        return JsonResponseEntity.success(accountService.register(account, accDto.getTenantId(), AccRoleEnum.valueOf(accDto.getAccRole())));
    }

    @Operation(summary = "渠道注册(无密码注册)")
    @PostMapping("/channelRegisterAndReturn")
    public JsonResponseEntity<AccVo> channelRegisterAndReturn(@RequestBody @Valid AccChannelRegisterDto accDto) {
        Account account = new Account();
        BeanUtils.copyProperties(accDto, account);
        if (StringUtils.isEmpty(accDto.getTenantId())) {
            accDto.setTenantId(UserContext.getTenant().getTenantId());
        }
        if (StringUtils.isEmpty(accDto.getPlatform())) {
            accDto.setPlatform(UserContext.getPlatForm());
        }
        accountService.register(account, accDto.getTenantId(), AccRoleEnum.valueOf(accDto.getAccRole()));
        return JsonResponseEntity.success(accountService.extraInfo(Account.covert(account, false)));
    }

    @Operation(summary = "修改密码")
    @PostMapping("/changePass")
    public JsonResponseEntity<Boolean> changePass(@RequestBody @Valid AccChangePassDto accChangePassDto) {
        if (StringUtils.isEmpty(accChangePassDto.getPlatform())) {
            accChangePassDto.setPlatform(UserContext.getPlatForm());
        }
        accountService.changePass(accChangePassDto);
        return JsonResponseEntity.success(true);
    }
}
