/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.line.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.line.model.ResetLineTFDto;
import com.ourexists.mesedge.line.model.query.LinePageQuery;
import com.ourexists.mesedge.line.pojo.Line;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface LineService extends IMyBatisPlusService<Line> {

    Page<Line> selectByPage(LinePageQuery dto);

    Line selectByCode(String code);

    List<Line> selectByCodes(List<String> codes);

    void resetLineTF(ResetLineTFDto dto);
}
