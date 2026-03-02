/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.line.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.line.model.ChangePriorityDto;
import com.ourexists.omes.line.pojo.TF;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface TFService extends IMyBatisPlusService<TF> {
    List<TF> selectByLineId(String lineId);

    void changePriority(ChangePriorityDto dto);
}
