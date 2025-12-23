/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ec.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.ec.model.EcRecordDto;
import com.ourexists.mesedge.ec.model.EcRecordQuery;
import com.ourexists.mesedge.ec.pojo.EcRecord;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface EcRecordService extends IMyBatisPlusService<EcRecord> {

    List<EcRecord> selectByCondition(EcRecordQuery dto);

    void addBatch(List<List<EcRecordDto>> dtoss);

    void delete(List<String> ids);
}
