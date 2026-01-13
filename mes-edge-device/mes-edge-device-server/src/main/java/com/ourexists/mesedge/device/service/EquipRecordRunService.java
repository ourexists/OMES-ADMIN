/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.device.model.EquipRecordRunDto;
import com.ourexists.mesedge.device.model.EquipRecordRunPageQuery;
import com.ourexists.mesedge.device.pojo.EquipRecordRun;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface EquipRecordRunService extends IMyBatisPlusService<EquipRecordRun> {

    Page<EquipRecordRun> selectByPage(EquipRecordRunPageQuery dto);

    void delete(List<String> ids);

    void add(EquipRecordRunDto dto);
}
