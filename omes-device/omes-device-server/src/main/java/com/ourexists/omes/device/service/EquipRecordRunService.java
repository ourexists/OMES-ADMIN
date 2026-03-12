/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.device.model.EquipRecordCountQuery;
import com.ourexists.omes.device.model.EquipRecordRunDto;
import com.ourexists.omes.device.model.EquipRecordRunPageQuery;
import com.ourexists.omes.device.model.EquipRecordRunVo;
import com.ourexists.omes.device.pojo.EquipRecordRun;

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

    List<EquipRecordRunVo> countMerging(EquipRecordCountQuery query);

    /** 某设备累计运行分钟数(全历史, state=1) */
    Long sumRunMinutesBySn(String sn);

    /** 某设备运行段数(全历史, state=1)，即启停总次数 */
    Long countRunSegmentsBySn(String sn);
}
