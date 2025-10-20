package com.ourexists.mesedge.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.report.model.WinCCDosingDev;
import com.ourexists.mesedge.report.model.WinCCDosingPageQuery;

public interface WinCCDosingDevService extends IMyBatisPlusService<WinCCDosingDev> {

    Page<WinCCDosingDev> selectByPage(WinCCDosingPageQuery dto);
}
