/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.portal.auth;

import com.ourexists.era.framework.core.EraSystemHeader;
import com.ourexists.mesedge.ucenter.account.AccVo;
import com.ourexists.mesedge.ucenter.account.pojo.Account;
import com.ourexists.mesedge.ucenter.account.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author pengcheng
 * @date 2022/4/14 15:47
 * @since 1.0.0
 */
@Service
public class PasswordDetailsServiceImpl extends CommonUserDetailService implements UserDetailsService {

    @Autowired
    private AccountService accountService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String platform = EraSystemHeader.extractPlatform(request);
        Account account = accountService.selectByAccName(username, platform);
        if (account == null) {
            throw new UsernameNotFoundException("${common.msg.username.error}");
        }
        AccVo accVo = Account.covert(account, false);
        accountService.extraInfo(accVo);
        if (StringUtils.isEmpty(accVo.getPassword())) {
            throw new UsernameNotFoundException("${common.msg.username.error}");
        }
        return eraUser(accVo);
    }
}
