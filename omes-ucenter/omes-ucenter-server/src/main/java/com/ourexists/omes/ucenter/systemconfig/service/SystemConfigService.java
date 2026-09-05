/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.ucenter.systemconfig.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.ucenter.systemconfig.SystemConfigDto;
import com.ourexists.omes.ucenter.systemconfig.pojo.SystemConfig;

public interface SystemConfigService extends IMyBatisPlusService<SystemConfig> {

    SystemConfigDto getAppConfig();

    void saveAppConfig(SystemConfigDto dto);

    String getBaiduMapAk();
}
