package com.ourexists.mesedge.report.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.report.model.WinCCDatalistDto;
import com.ourexists.mesedge.report.model.WinCCDatalistPageQuery;

import java.util.List;

public interface WinCCReportFeign {

    JsonResponseEntity<Boolean> save(WinCCDatalistDto winCCDatalistDto);

    JsonResponseEntity<List<WinCCDatalistDto>> selectByPage(WinCCDatalistPageQuery dto);
}
