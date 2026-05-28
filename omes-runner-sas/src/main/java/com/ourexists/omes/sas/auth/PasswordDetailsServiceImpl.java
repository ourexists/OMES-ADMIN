/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.sas.auth;

import com.ourexists.era.framework.core.EraSystemHeader;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.sas.remote.RemoteAccountAuthClient;
import com.ourexists.omes.ucenter.account.AccVo;
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
    private RemoteAccountAuthClient accountAuthClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String platform = EraSystemHeader.extractPlatform(request);
        AccVo accVo;
        try {
            accVo = RemoteHandleUtils.getDataFormResponse(
                    accountAuthClient.selectByAccname(username, platform));
        } catch (EraCommonException e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
        if (accVo == null) {
            throw new UsernameNotFoundException("${common.msg.username.error}");
        }
        if (StringUtils.isEmpty(accVo.getPassword())) {
            throw new UsernameNotFoundException("${common.msg.username.error}");
        }
        return eraUser(accVo);
    }
}
