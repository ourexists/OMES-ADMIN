/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.inspection.model.InspectTemplatePageQuery;
import com.ourexists.omes.inspection.pojo.InspectTemplate;

import java.util.List;

public interface InspectTemplateService extends IMyBatisPlusService<InspectTemplate> {

    Page<InspectTemplate> selectByPage(InspectTemplatePageQuery query);

    List<InspectTemplate> selectList();
}
