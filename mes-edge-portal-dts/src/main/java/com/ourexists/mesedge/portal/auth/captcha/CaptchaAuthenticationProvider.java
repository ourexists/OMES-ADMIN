/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.auth.captcha;

import com.ourexists.era.framework.webserver.authorization.EraAuthenticationProvider;
import com.ourexists.era.oauth2.core.token.EraAuthenticationToken;
import com.ourexists.mesedge.portal.auth.AuthValidRuleCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;


public class CaptchaAuthenticationProvider implements EraAuthenticationProvider {

    private static final long MAX_VALIDATE_NUM = 5;

    private final UserDetailsService userDetailsService;

    private final AuthValidRuleCache authValidRuleCache;

    private final PasswordEncoder passwordEncoder;

    public CaptchaAuthenticationProvider(UserDetailsService userDetailsService,
                                         AuthValidRuleCache authValidRuleCache,
                                         PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.authValidRuleCache = authValidRuleCache;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CaptchaAuthenticationToken authenticationToken = (CaptchaAuthenticationToken) authentication;

        String cap = authenticationToken.getCaptcha();
        if (StringUtils.isEmpty(cap)) {
            throw new BadCredentialsException("图形码验证失败！");
        }
        String[] caps = cap.split("-");
        if (caps.length < 2) {
            throw new BadCredentialsException("图形码验证失败！");
        }
        String uuid = caps[0];
        String captcha = caps[1];
        String hcaptcha = authValidRuleCache.getCaptcha(uuid);
        if (hcaptcha == null) {
            throw new BadCredentialsException("图形码失效！");
        }
        if (!hcaptcha.equals(captcha)) {
            authValidRuleCache.removeCaptcha(uuid);
            throw new BadCredentialsException("图形码验证失败！");
        }
        String username = authenticationToken.getUsername();
        String password = authenticationToken.getPassword();
        if (authValidRuleCache.wrongNum(username) > MAX_VALIDATE_NUM) {
            throw new LockedException("密码验证错误次数过多，账户已被冻结，" + AuthValidRuleCache.ACCOUNT_WRONG_LOCK_DURATION_HOUR + "小时后解冻");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            long errNum = authValidRuleCache.accumulatesError(username);
            if (errNum > MAX_VALIDATE_NUM) {
                throw new BadCredentialsException("验证超过上限次数，账户将被冻结" + AuthValidRuleCache.ACCOUNT_WRONG_LOCK_DURATION_HOUR + "小时!");
            }
            throw new BadCredentialsException("账户名密码错误！");
        }
        if (!userDetails.isEnabled()) {
            throw new UsernameNotFoundException("该账户不存在！");
        }
        if (!userDetails.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException("验证票据过期！");
        }
        if (!userDetails.isAccountNonExpired()) {
            throw new AccountExpiredException("账户过期！");
        }
        if (!userDetails.isAccountNonLocked()) {
            throw new LockedException("账户已被锁定，请找管理员解锁！");
        }

        EraAuthenticationToken eraAuthenticationToken = new EraAuthenticationToken(userDetails, userDetails.getAuthorities());
        eraAuthenticationToken.setDetails(authentication.getDetails());
        return eraAuthenticationToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CaptchaAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
