package com.ourexists.mesedge.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.report.model.WinCCWpsDev;
import com.ourexists.mesedge.report.model.WinCCWpsPageQuery;

public interface WinCCWpsDevService extends IMyBatisPlusService<WinCCWpsDev> {

    Page<WinCCWpsDev> selectByPage(WinCCWpsPageQuery dto);
}
