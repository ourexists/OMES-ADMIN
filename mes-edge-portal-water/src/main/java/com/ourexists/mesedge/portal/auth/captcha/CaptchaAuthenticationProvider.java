/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.auth.captcha;

import com.ourexists.mesedge.portal.auth.AuthValidRuleCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.util.Set;


public class CaptchaAuthenticationProvider implements AuthenticationProvider {

    private static final long MAX_VALIDATE_NUM = 5;

    private final UserDetailsService userDetailsService;

    private final AuthValidRuleCache authValidRuleCache;

    private final PasswordEncoder passwordEncoder;

    private final RegisteredClientRepository registeredClientRepository;

    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    private final OAuth2AuthorizationService authorizationService;

    public CaptchaAuthenticationProvider(UserDetailsService userDetailsService,
                                         AuthValidRuleCache authValidRuleCache,
                                         PasswordEncoder passwordEncoder,
                                         RegisteredClientRepository registeredClientRepository,
                                         OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
                                         OAuth2AuthorizationService authorizationService) {
        this.userDetailsService = userDetailsService;
        this.authValidRuleCache = authValidRuleCache;
        this.passwordEncoder = passwordEncoder;
        this.registeredClientRepository = registeredClientRepository;
        this.tokenGenerator = tokenGenerator;
        this.authorizationService = authorizationService;
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
        RegisteredClient client = registeredClientRepository.findByClientId(authenticationToken.getClientId());
        if (client == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }

        Authentication principal = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        Set<String> scopes = client.getScopes();
        OAuth2TokenContext context = DefaultOAuth2TokenContext.builder()
                .registeredClient(client)
                .principal(principal)
                .authorizationGrantType(new AuthorizationGrantType(CaptchaAuthenticationConverter.GRANT_TYPE))
                .authorizationGrant(authenticationToken)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .authorizedScopes(scopes)
                .build();
        OAuth2Token token = tokenGenerator.generate(context);
        if (token == null) {
            throw new OAuth2AuthenticationException("token_generator_failed");
        }
        OAuth2AccessToken accessToken;
        if (token instanceof OAuth2AccessToken) {
            accessToken = (OAuth2AccessToken) token;
        } else if (token instanceof Jwt jwt) {
            accessToken = new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    jwt.getTokenValue(),
                    jwt.getIssuedAt(),
                    jwt.getExpiresAt(),
                    context.getAuthorizedScopes()
            );
        } else {
            throw new OAuth2AuthenticationException("unsupported_token_type");
        }
        OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(client)
                .principalName(principal.getName())
                .authorizationGrantType(context.getAuthorizationGrantType())
                .authorizedScopes(scopes)
                .token(accessToken)
                .build();
        authorizationService.save(authorization);

        return new OAuth2AccessTokenAuthenticationToken(
                client, principal, accessToken, null
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CaptchaAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
