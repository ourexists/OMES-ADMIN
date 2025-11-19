package com.ourexists.mesedge.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.report.model.WinCCZsDev;
import com.ourexists.mesedge.report.model.WinCCZsPageQuery;

public interface WinCCZsDevService extends IMyBatisPlusService<WinCCZsDev> {

    Page<WinCCZsDev> selectByPage(WinCCZsPageQuery dto);
}
