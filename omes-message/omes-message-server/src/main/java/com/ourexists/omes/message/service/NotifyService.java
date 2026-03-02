/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.message.model.NotifyDto;
import com.ourexists.omes.message.model.query.NotifyPageQuery;
import com.ourexists.omes.message.pojo.Notify;

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

    void createAndStart(NotifyDto dto);
}
