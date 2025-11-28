/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.ucenter.platform.PlatformPageQuery;
import com.ourexists.mesedge.ucenter.platform.mapper.PlatformMapper;
import com.ourexists.mesedge.ucenter.platform.pojo.Platform;
import com.ourexists.mesedge.ucenter.platform.service.PlatformService;
import com.ourexists.mesedge.ucenter.role.RolePageQuery;
import org.springframework.stereotype.Service;


/**
 * @author pengcheng
 * @date 2022/4/2 16:20
 * @since 1.0.0
 */
@Service
public class PlatformServiceImpl extends AbstractMyBatisPlusService<PlatformMapper, Platform> implements PlatformService {
}
