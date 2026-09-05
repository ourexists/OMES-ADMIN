/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.ucenter.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.ucenter.systemconfig.SystemConfigDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

public interface SystemConfigFeign {

    JsonResponseEntity<SystemConfigDto> getAppConfig();

    JsonResponseEntity<Boolean> saveAppConfig(@Validated @RequestBody SystemConfigDto dto);

    JsonResponseEntity<String> getBaiduMapAk();
}
