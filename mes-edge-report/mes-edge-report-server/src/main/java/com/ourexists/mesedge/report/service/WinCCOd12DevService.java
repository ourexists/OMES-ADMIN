package com.ourexists.mesedge.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.report.model.WinCCOd12Dev;
import com.ourexists.mesedge.report.model.WinCCOd12PageQuery;

public interface WinCCOd12DevService extends IMyBatisPlusService<WinCCOd12Dev> {

    Page<WinCCOd12Dev> selectByPage(WinCCOd12PageQuery dto);
}
