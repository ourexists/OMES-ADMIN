/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.message.enums.MessageReadEnum;
import com.ourexists.mesedge.message.mapper.MessageMapper;
import com.ourexists.mesedge.message.mapper.MessageReadMapper;
import com.ourexists.mesedge.message.model.MessageDto;
import com.ourexists.mesedge.message.model.MessageVo;
import com.ourexists.mesedge.message.model.query.MessagePageQuery;
import com.ourexists.mesedge.message.pojo.Message;
import com.ourexists.mesedge.message.pojo.MessageRead;
import com.ourexists.mesedge.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MessageServiceImpl extends AbstractMyBatisPlusService<MessageMapper, Message> implements MessageService {

    @Autowired
    private MessageReadMapper messageReadMapper;

    @Override
    public Page<MessageVo> selectByPage(MessagePageQuery dto) {
        LambdaQueryWrapper<Message> qw = new LambdaQueryWrapper<Message>()
                .eq(dto.getType() != null, Message::getType, dto.getType())
                .eq(StringUtils.hasText(dto.getPlatform()), Message::getPlatform, dto.getPlatform())
                .ge(dto.getCreatedTimeStart() != null, Message::getCreatedTime, dto.getCreatedTimeStart())
                .lt(dto.getCreatedTimeEnd() != null, Message::getCreatedTime, dto.getCreatedTimeEnd())
                .inSql(StringUtils.hasText(dto.getAccId()) && dto.getReadStatus() == null, Message::getId, "select message_id from r_message_read where acc_id=" + dto.getAccId())
                .inSql(StringUtils.hasText(dto.getAccId()) && MessageReadEnum.read.getCode().equals(dto.getReadStatus()), Message::getId, "select message_id from r_message_read where is_read=true and acc_id=" + dto.getAccId())
                .inSql(StringUtils.hasText(dto.getAccId()) && MessageReadEnum.unread.getCode().equals(dto.getReadStatus()), Message::getId, "select message_id from r_message_read where is_read=false and acc_id=" + dto.getAccId())
                .orderByDesc(Message::getId);
        Page<Message> page = this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
        List<MessageVo> r = Message.covert(page.getRecords());
        if (StringUtils.hasText(dto.getAccId()) && !CollectionUtils.isEmpty(r)) {
            List<String> messageIds = r.stream().map(MessageVo::getId).toList();
            List<MessageRead> messageReads = messageReadMapper.selectList(new LambdaQueryWrapper<MessageRead>()
                    .eq(MessageRead::getAccId, dto.getAccId())
                    .in(MessageRead::getMessageId, messageIds)
            );
            if (!CollectionUtils.isEmpty(messageReads)) {
                for (MessageVo messageVo : r) {
                    for (MessageRead messageRead : messageReads) {
                        if (messageVo.getId().equals(messageRead.getMessageId())) {
                            messageVo.setReadStatus(messageRead.getIsRead() ? MessageReadEnum.read.getCode() : MessageReadEnum.unread.getCode());
                            messageVo.setReadTime(messageRead.getTime());
                            break;
                        }
                    }
                }
            }
        }
        Page<MessageVo> pager = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        pager.setRecords(r);
        return pager;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<String> ids) {
        this.removeByIds(ids);
        this.messageReadMapper.delete(new LambdaQueryWrapper<MessageRead>().in(MessageRead::getMessageId, ids));
    }

    @Override
    public void read(String messageId) {
        this.messageReadMapper.update(
                new LambdaUpdateWrapper<MessageRead>()
                        .set(MessageRead::getIsRead, true)
                        .set(MessageRead::getTime, new Date())
                        .eq(MessageRead::getMessageId, messageId)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVo produce(MessageDto dto) {
        Message message = Message.wrap(dto);
        if (this.save(message)) {
            if (!CollectionUtils.isEmpty(dto.getSendAccounts())) {
                List<MessageRead> messageReads = new ArrayList<>();
                for (String sendAccount : dto.getSendAccounts()) {
                    MessageRead messageRead = new MessageRead()
                            .setIsRead(false)
                            .setAccId(sendAccount)
                            .setMessageId(message.getId());
                    messageReads.add(messageRead);
                }
                this.messageReadMapper.insert(messageReads);
            }
        }
        return Message.covert(message);
    }

    @Override
    public MessageVo selectById(String id, String accId) {
        MessageVo messageVo = Message.covert(this.getById(id));
        if (messageVo != null) {
            MessageRead messageRead = messageReadMapper.selectOne(new LambdaQueryWrapper<MessageRead>()
                    .eq(MessageRead::getMessageId, id)
                    .eq(MessageRead::getAccId, accId));
            messageVo.setReadStatus(messageRead.getIsRead() ? MessageReadEnum.read.getCode() : MessageReadEnum.unread.getCode());
            messageVo.setReadTime(messageRead.getTime());
        }
        return messageVo;
    }

    @Override
    public Long countReadStatus(String userId, String platform, Integer readStatus) {
        return this.messageReadMapper.selectCount(new LambdaQueryWrapper<MessageRead>()
                .eq(MessageRead::getAccId, userId)
                .eq(MessageRead::getIsRead, readStatus == 1)
                .inSql(MessageRead::getMessageId, "select id from t_message where platform='" + platform + "'")
        );
    }
}
