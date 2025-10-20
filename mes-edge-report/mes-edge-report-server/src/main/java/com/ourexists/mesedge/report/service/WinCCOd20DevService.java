package com.ourexists.mesedge.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.report.model.WinCCOd20Dev;
import com.ourexists.mesedge.report.model.WinCCOd20PageQuery;

public interface WinCCOd20DevService extends IMyBatisPlusService<WinCCOd20Dev> {

    Page<WinCCOd20Dev> selectByPage(WinCCOd20PageQuery dto);
}
