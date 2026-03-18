/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.inspection.model.InspectPersonPageQuery;
import com.ourexists.omes.inspection.pojo.InspectPerson;

public interface InspectPersonService extends IMyBatisPlusService<InspectPerson> {

    Page<InspectPerson> selectByPage(InspectPersonPageQuery query);
}
