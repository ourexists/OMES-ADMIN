/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.sas.auth;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.util.RandomUtil;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Tag(name = "认证相关")
@RestController
@RequestMapping("/open")
public class OpenController {

    @Autowired
    private AuthValidRuleCache authValidRuleCache;

    @Operation(summary = "验证码")
    @GetMapping("/captcha")
    public JsonResponseEntity<Boolean> captcha(@RequestParam String uuid,
                                               HttpServletResponse response) {
        CircleCaptcha lineCaptcha = CaptchaUtil.createCircleCaptcha(100, 50);
        authValidRuleCache.setCaptcha(uuid, lineCaptcha.getCode());
        try (ServletOutputStream out = response.getOutputStream()) {
            lineCaptcha.write(out);
        } catch (IOException e) {
            throw new BusinessException("图形验证码生成失败!");
        }
        return JsonResponseEntity.success(true);
    }


    @Operation(summary = "验证码")
    @GetMapping("/captchaBase")
    public JsonResponseEntity<String> captchaBase(@RequestParam String uuid) {
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(100, 50);
        authValidRuleCache.setCaptcha(uuid, captcha.getCode());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        captcha.write(baos);  // 将验证码图片写入输出流
        String base64Img = Base64.getEncoder().encodeToString(baos.toByteArray());
        return JsonResponseEntity.success("data:image/png;base64," + base64Img);
    }

    @Operation(summary = "滑块验证码初始化")
    @GetMapping("/captchaSlider")
    public JsonResponseEntity<Boolean> captchaSlider(@RequestParam String uuid) {
        String code = RandomUtil.randomString(RandomUtil.BASE_CHAR_NUMBER_LOWER, 6);
        authValidRuleCache.setCaptcha(uuid, code);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "滑块验证码校验")
    @PostMapping("/captchaSlider/verify")
    public JsonResponseEntity<String> verifyCaptchaSlider(@RequestParam String uuid,
                                                          @RequestParam int offset,
                                                          @RequestParam int trackWidth) {
        String code = authValidRuleCache.getCaptcha(uuid);
        if (code == null) {
            throw new BusinessException("验证码失效，请刷新后重试");
        }
        if (trackWidth <= 0) {
            authValidRuleCache.removeCaptcha(uuid);
            throw new BusinessException("滑动验证失败");
        }
        int thumbWidth = 44;
        int maxOffset = Math.max(0, trackWidth - thumbWidth);
        double passRatio = 0.92;
        if (offset < maxOffset * passRatio) {
            authValidRuleCache.removeCaptcha(uuid);
            throw new BusinessException("滑动验证失败，请重试");
        }
        return JsonResponseEntity.success(code);
    }
}
