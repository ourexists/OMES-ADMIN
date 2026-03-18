/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.inspection.model.InspectRecordPageQuery;
import com.ourexists.omes.inspection.model.InspectRecordSaveRequest;
import com.ourexists.omes.inspection.pojo.InspectRecord;

import java.util.Date;
import java.util.List;

public interface InspectRecordService extends IMyBatisPlusService<InspectRecord> {

    Page<InspectRecord> selectByPage(InspectRecordPageQuery query);

    List<InspectRecord> listByTaskId(String taskId);

    /**
     * 按设备ID与记录时间范围查询巡检记录（用于设备健康分巡检维度统计）
     */
    List<InspectRecord> listByEquipIdAndRecordTimeBetween(String equipId, Date recordTimeStart, Date recordTimeEnd);

    /**
     * 生成巡检记录（APP 提交：按任务+设备+各检测项结果批量落库）
     */
    void saveRecord(InspectRecordSaveRequest request);
}
