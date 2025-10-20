package com.ourexists.mesedge.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.report.model.WinCCOd11Dev;
import com.ourexists.mesedge.report.model.WinCCOd11PageQuery;

public interface WinCCOd11DevService extends IMyBatisPlusService<WinCCOd11Dev> {

    Page<WinCCOd11Dev> selectByPage(WinCCOd11PageQuery dto);
}
