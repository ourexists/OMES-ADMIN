/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.device.model.EquipStateSnapshotCountDto;
import com.ourexists.mesedge.device.model.EquipStateSnapshotCountQuery;
import com.ourexists.mesedge.device.model.EquipStateSnapshotDto;
import com.ourexists.mesedge.device.model.EquipStateSnapshotPageQuery;
import com.ourexists.mesedge.device.pojo.EquipStateSnapshot;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface EquipStateSnapshotService extends IMyBatisPlusService<EquipStateSnapshot> {

    Page<EquipStateSnapshot> selectByPage(EquipStateSnapshotPageQuery dto);

    void delete(List<String> ids);

    void add(EquipStateSnapshotDto dto);

    void addBatch(List<EquipStateSnapshotDto> dtos);

    List<EquipStateSnapshotCountDto> countNumByTime(EquipStateSnapshotCountQuery dto);
}
