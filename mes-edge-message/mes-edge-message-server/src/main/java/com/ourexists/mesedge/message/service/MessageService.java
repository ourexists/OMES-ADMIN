/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.message.model.MessageDto;
import com.ourexists.mesedge.message.model.MessageVo;
import com.ourexists.mesedge.message.model.query.MessagePageQuery;
import com.ourexists.mesedge.message.pojo.Message;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface MessageService extends IMyBatisPlusService<Message> {

    Page<MessageVo> selectByPage(MessagePageQuery dto);

    void deleteByIds(List<String> ids);

    void read(String messageId);

    MessageVo produce(MessageDto dto);

    MessageVo selectById(String id, String accId);
}
