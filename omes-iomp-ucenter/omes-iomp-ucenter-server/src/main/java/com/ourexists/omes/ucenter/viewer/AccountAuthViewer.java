/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.ucenter.viewer;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.omes.ucenter.account.*;
import com.ourexists.omes.ucenter.account.pojo.Account;
import com.ourexists.omes.ucenter.account.service.AccountService;
import com.ourexists.omes.ucenter.enums.AccRoleEnum;
import com.ourexists.omes.ucenter.feign.AccountAuthFeign;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 认证场景账户数据实现，供管理端与 SAS 远程调用。
 */
@Component
public class AccountAuthViewer implements AccountAuthFeign {

    @Autowired
    private AccountService accountService;

    @Override
    public JsonResponseEntity<AccVo> selectByAccname(@RequestParam String accname,
                                                   @RequestParam(required = false) String platform) {
        String plat = resolvePlatform(platform);
        Account account = accountService.selectByAccName(accname, plat);
        if (account == null) {
            return JsonResponseEntity.success(null);
        }
        AccVo accVo = Account.covert(account, false);
        return JsonResponseEntity.success(accountService.extraInfo(accVo));
    }

    @Override
    public JsonResponseEntity<AccVo> selectByMobile(@RequestParam String mobile,
                                                  @RequestParam(required = false) String platform) {
        String plat = resolvePlatform(platform);
        Account account = accountService.selectByMobile(mobile, plat);
        if (account == null) {
            return JsonResponseEntity.success(null);
        }
        return JsonResponseEntity.success(accountService.extraInfo(Account.covert(account, false)));
    }

    @Override
    public JsonResponseEntity<String> register(@RequestBody @Valid AccRegisterDto accDto) {
        return JsonResponseEntity.success(accountService.register(
                Account.warp(accDto), accDto.getTenantId(), AccRoleEnum.valueOf(accDto.getAccRole())));
    }

    @Override
    public JsonResponseEntity<String> channelRegister(@RequestBody @Valid AccChannelRegisterDto accDto) {
        Account account = new Account();
        BeanUtils.copyProperties(accDto, account);
        return JsonResponseEntity.success(accountService.register(
                account, accDto.getTenantId(), AccRoleEnum.valueOf(accDto.getAccRole())));
    }

    @Override
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

    @Override
    public JsonResponseEntity<Boolean> changePass(@RequestBody @Valid AccChangePassDto accChangePassDto) {
        if (StringUtils.isEmpty(accChangePassDto.getPlatform())) {
            accChangePassDto.setPlatform(UserContext.getPlatForm());
        }
        accountService.changePass(accChangePassDto);
        return JsonResponseEntity.success(true);
    }

    private static String resolvePlatform(String platform) {
        if (StringUtils.isNotEmpty(platform)) {
            return platform;
        }
        return UserContext.getPlatForm();
    }
}
