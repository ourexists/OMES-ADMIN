/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.message.model.query.NotifyPageQuery;
import com.ourexists.mesedge.message.pojo.Notify;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface NotifyService extends IMyBatisPlusService<Notify> {

    Page<Notify> selectByPage(NotifyPageQuery dto);

    void addOrUpdate(Notify wrap);

    void delete(List<String> ids);

    void updateStatus(String id, Integer status);
}
