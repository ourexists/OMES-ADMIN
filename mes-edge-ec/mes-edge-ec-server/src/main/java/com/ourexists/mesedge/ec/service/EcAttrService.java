/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ec.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.ec.model.EcAttrBatchDto;
import com.ourexists.mesedge.ec.model.EcAttrDto;
import com.ourexists.mesedge.ec.model.EcAttrPageQuery;
import com.ourexists.mesedge.ec.pojo.EcAttr;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface EcAttrService extends IMyBatisPlusService<EcAttr> {

    Page<EcAttr> selectByPage(EcAttrPageQuery dto);

    void addOrUpdate(EcAttrDto dto);

    void delete(List<String> ids);

    void insertBatch(EcAttrBatchDto dto);
}
