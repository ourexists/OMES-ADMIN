/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.device.model.DevgDto;
import com.ourexists.mesedge.device.model.DevgPageQuery;
import com.ourexists.mesedge.device.pojo.Devg;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface DevgService extends IMyBatisPlusService<Devg> {

    Page<Devg> selectByPage(DevgPageQuery dto);

    void addOrUpdate(DevgDto dto);

    void delete(List<String> ids);
}
