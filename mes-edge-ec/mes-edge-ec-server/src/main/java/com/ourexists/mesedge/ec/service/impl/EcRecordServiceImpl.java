/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ec.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.ec.mapper.EcRecordMapper;
import com.ourexists.mesedge.ec.model.EcRecordDto;
import com.ourexists.mesedge.ec.model.EcRecordQuery;
import com.ourexists.mesedge.ec.pojo.EcRecord;
import com.ourexists.mesedge.ec.service.EcRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class EcRecordServiceImpl extends AbstractMyBatisPlusService<EcRecordMapper, EcRecord> implements EcRecordService {

    @Override
    public List<EcRecord> selectByCondition(EcRecordQuery dto) {
        return this.list(new LambdaUpdateWrapper<EcRecord>()
                .in(!CollectionUtils.isEmpty(dto.getAttrIds()), EcRecord::getAttrId, dto.getAttrIds())
                .between(dto.getStartTime() != null && dto.getEndTime() != null, EcRecord::getTime, dto.getStartTime(), dto.getEndTime())
                .orderByAsc(EcRecord::getTime, EcRecord::getRecordId)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBatch(List<List<EcRecordDto>> dtoss) {
        if (CollectionUtils.isEmpty(dtoss)) {
            List<EcRecord> lst = new ArrayList<>();
            for (List<EcRecordDto> dtos : dtoss) {
                Date time = new Date();
                String group = IdWorker.getIdStr();
                for (EcRecordDto dto : dtos) {
                    if (dto.getTime() == null) {
                        dto.setTime(time);
                    }
                    if (dto.getRecordId() == null) {
                        dto.setRecordId(group);
                    }
                }
                lst.addAll(EcRecord.wrap(dtos));
            }
            this.saveBatch(lst);
        }
    }

    @Override
    public void delete(List<String> ids) {

    }
}
