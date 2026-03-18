/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.inspection.pojo.InspectRecordItem;

import java.util.List;

/**
 * 巡检记录明细（附表）
 */
public interface InspectRecordItemService extends IMyBatisPlusService<InspectRecordItem> {

    /**
     * 按记录ID列表批量查询明细，按 recordId 分组返回（用于主表+附表组装）
     */
    List<InspectRecordItem> listByRecordIds(List<String> recordIds);
}
