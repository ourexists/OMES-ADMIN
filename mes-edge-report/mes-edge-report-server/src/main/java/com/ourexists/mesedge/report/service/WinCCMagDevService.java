package com.ourexists.mesedge.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.report.model.WinCCMagDev;
import com.ourexists.mesedge.report.model.WinCCMagPageQuery;

public interface WinCCMagDevService extends IMyBatisPlusService<WinCCMagDev> {

    Page<WinCCMagDev> selectByPage(WinCCMagPageQuery dto);
}
