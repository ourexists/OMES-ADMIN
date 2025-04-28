/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.auth;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Tag(name = "认证相关")
@RestController
@RequestMapping("/open")
public class OpenController {

    @Autowired
    private AuthCacheManager authCacheManager;

    @Operation(summary = "验证码")
    @GetMapping("/captcha")
    public JsonResponseEntity<Boolean> captcha(@RequestParam String uuid,
                                               HttpServletResponse response) {
        CircleCaptcha lineCaptcha = CaptchaUtil.createCircleCaptcha(100, 50);
        authCacheManager.setCaptcha(uuid, lineCaptcha.getCode());
        try (ServletOutputStream out = response.getOutputStream()) {
            lineCaptcha.write(out);
        } catch (IOException e) {
            throw new BusinessException("图形验证码生成失败!");
        }
        return JsonResponseEntity.success(true);
    }
}
