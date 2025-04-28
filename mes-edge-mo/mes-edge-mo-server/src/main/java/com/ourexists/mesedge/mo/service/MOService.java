/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.mo.model.MODto;
import com.ourexists.mesedge.mo.enums.MOStatusEnum;
import com.ourexists.mesedge.mo.pojo.MO;
import com.ourexists.mesedge.mo.model.query.MOPageQuery;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface MOService extends IMyBatisPlusService<MO> {

    Page<MO> selectByPage(MOPageQuery dto);

    List<MO> selectByCodes(List<String> codes);

    MO selectByCode(String code);

    void addOrUpdate(MODto wrap);

    void addBatch(List<MODto> moDtos);

    void delete(List<String> ids);

    void updateStatus(List<String> moCodes, MOStatusEnum moStatusEnum);

    void updateSurplus(String code, int surplus);
}
