/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.message.core.NotifyPusher;
import com.ourexists.mesedge.message.enums.NotifyStatusEnum;
import com.ourexists.mesedge.message.mapper.NotifyMapper;
import com.ourexists.mesedge.message.model.NotifyDto;
import com.ourexists.mesedge.message.model.query.NotifyPageQuery;
import com.ourexists.mesedge.message.pojo.Notify;
import com.ourexists.mesedge.message.service.NotifyService;
import com.ourexists.mesedge.message.timer.NotifyTimerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class NotifyServiceImpl extends AbstractMyBatisPlusService<NotifyMapper, Notify> implements NotifyService {

    @Autowired
    private NotifyTimerManager notifyTimerManager;

    @Autowired
    private NotifyPusher notifyPusher;

    @Override
    public Page<Notify> selectByPage(NotifyPageQuery dto) {
        LambdaQueryWrapper<Notify> qw = new LambdaQueryWrapper<Notify>()
                .eq(dto.getStatus() != null, Notify::getStatus, dto.getStatus())
                .eq(dto.getType() != null, Notify::getType, dto.getType())
                .like(StringUtils.hasText(dto.getPlatform()), Notify::getPlatform, "%" + dto.getPlatform() + "%")
                .orderByDesc(Notify::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    public void addOrUpdate(Notify notify) {
        notify.setStatus(NotifyStatusEnum.READY.getCode());
        if (StringUtils.hasText(notify.getId())) {
            Notify source = this.getById(notify.getId());
            if (source == null) {
                throw new BusinessException("The data to be operated on does not exist.");
            }
            if (!source.getStatus().equals(NotifyStatusEnum.READY.getCode())) {
                throw new BusinessException("Current status can not be changed.");
            }
        }
        this.saveOrUpdate(notify);
    }

    @Override
    public void delete(List<String> ids) {
        this.removeByIds(ids);
        for (String id : ids) {
            notifyTimerManager.removeTask(id);
        }
    }

    @Override
    public void updateStatus(String id, Integer status) {
        Notify source = this.getById(id);
        if (source == null) {
            throw new BusinessException("The data to be operated on does not exist.");
        }
        boolean r = this.update(new LambdaUpdateWrapper<Notify>()
                .set(Notify::getStatus, status)
                .eq(Notify::getId, id)
                .ne(Notify::getStatus, status)
        );
        if (!r) {
            return;
        }
        //首次变为执行中会发送一条
        if (status.equals(NotifyStatusEnum.PROGRESS.getCode())) {
            notifyPusher.push(Notify.covertMsg(source));
            if (source.getStep() > 0) {
                notifyTimerManager.addTask(id, source.getStep());
            } else {
                this.update(new LambdaUpdateWrapper<Notify>()
                        .set(Notify::getStatus, NotifyStatusEnum.COMPLETED.getCode())
                        .eq(Notify::getId, id));
            }
        }
        if (status.equals(NotifyStatusEnum.COMPLETED.getCode())) {
            notifyTimerManager.removeTask(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createAndStart(NotifyDto dto) {
        Notify notify = Notify.wrap(dto);
        notify.setId(null);
        this.addOrUpdate(notify);
        this.updateStatus(notify.getId(), NotifyStatusEnum.PROGRESS.getCode());
    }
}
