/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.inspection.pojo.InspectTemplateItem;

import java.util.List;

public interface InspectTemplateItemService extends IMyBatisPlusService<InspectTemplateItem> {

    List<InspectTemplateItem> listByTemplateId(String templateId);

    void saveBatch(String templateId, List<InspectTemplateItem> items);

    void deleteByTemplateId(String templateId);
}

