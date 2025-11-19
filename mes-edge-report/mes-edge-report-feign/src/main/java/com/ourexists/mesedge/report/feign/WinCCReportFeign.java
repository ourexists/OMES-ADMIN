package com.ourexists.mesedge.report.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.report.model.*;

import java.util.List;

public interface WinCCReportFeign {

    JsonResponseEntity<Boolean> saveDataList(WinCCDatalistDto winCCDatalistDto);

    JsonResponseEntity<WinCCDatalistResDto> selectDataListByPage(WinCCDatalistPageQuery dto) throws IllegalAccessException;

    JsonResponseEntity<Boolean> saveDosing(WinCCDosingDevDto dto);

    JsonResponseEntity<List<WinCCDosingDevDto>> selectDosingByPage(WinCCDosingPageQuery dto);

    JsonResponseEntity<Boolean> saveMag(WinCCMagDevDto dto);

    JsonResponseEntity<List<WinCCMagDevDto>> selectMagByPage(WinCCMagPageQuery dto);

    JsonResponseEntity<Boolean> saveOd11(WinCCOd11DevDto dto);

    JsonResponseEntity<List<WinCCOd11DevDto>> selectOd11ByPage(WinCCOd11PageQuery dto);

    JsonResponseEntity<Boolean> saveOd12(WinCCOd12DevDto dto);

    JsonResponseEntity<List<WinCCOd12DevDto>> selectOd12ByPage(WinCCOd12PageQuery dto);

    JsonResponseEntity<Boolean> saveOd20(WinCCOd20DevDto dto);

    JsonResponseEntity<List<WinCCOd20DevDto>> selectOd20ByPage(WinCCOd20PageQuery dto);

    JsonResponseEntity<Boolean> saveWps(WinCCWpsDevDto dto);

    JsonResponseEntity<List<WinCCWpsDevDto>> selectWpsByPage(WinCCWpsPageQuery dto);

    JsonResponseEntity<Boolean> saveZs(WinCCZsDevDto dto);

    JsonResponseEntity<List<WinCCZsDevDto>> selectZsByPage(WinCCZsPageQuery dto);
}
