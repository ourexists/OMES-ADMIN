package com.ourexists.mesedge.report.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.report.feign.WinCCReportFeign;
import com.ourexists.mesedge.report.model.WinCCDatalist;
import com.ourexists.mesedge.report.model.WinCCDatalistDto;
import com.ourexists.mesedge.report.model.WinCCDatalistPageQuery;
import com.ourexists.mesedge.report.service.WinCCDatalistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WinCCReportViewer implements WinCCReportFeign {

    @Autowired
    private WinCCDatalistService winCCDatalistService;

    public JsonResponseEntity<Boolean> save(WinCCDatalistDto winCCDatalistDto) {
        winCCDatalistService.save(WinCCDatalist.wrap(winCCDatalistDto));
        return JsonResponseEntity.success(true);
    }

    public JsonResponseEntity<List<WinCCDatalistDto>> selectByPage(WinCCDatalistPageQuery dto) {
        Page<WinCCDatalist> page = winCCDatalistService.selectByPage(dto);
        return JsonResponseEntity.success(WinCCDatalist.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }
}
