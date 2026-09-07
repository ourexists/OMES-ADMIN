/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.inspection.model.InspectItemPageQuery;
import com.ourexists.omes.inspection.pojo.InspectItem;

import java.util.List;

public interface InspectItemService extends IMyBatisPlusService<InspectItem> {

    Page<InspectItem> selectByPage(InspectItemPageQuery query);

    /** 公共库巡检项（未绑定模板的项，供模板「从巡检项载入」使用） */
    List<InspectItem> listAllPool();
}
