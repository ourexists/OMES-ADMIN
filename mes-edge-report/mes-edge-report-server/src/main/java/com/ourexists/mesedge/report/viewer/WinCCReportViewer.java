package com.ourexists.mesedge.report.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.report.feign.WinCCReportFeign;
import com.ourexists.mesedge.report.model.*;
import com.ourexists.mesedge.report.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;

@Component
public class WinCCReportViewer implements WinCCReportFeign {

    @Autowired
    private WinCCDatalistService winCCDatalistService;

    @Autowired
    private WinCCDosingDevService winCCDosingDevService;

    @Autowired
    private WinCCMagDevService winCCMagDevService;

    @Autowired
    private WinCCOd11DevService winCCOd11DevService;

    @Autowired
    private WinCCOd12DevService winCCOd12DevService;

    @Autowired
    private WinCCOd20DevService winCCOd20DevService;

    @Autowired
    private WinCCWpsDevService winCCWpsDevService;

    public JsonResponseEntity<Boolean> saveDataList(WinCCDatalistDto winCCDatalistDto) {
        winCCDatalistService.save(WinCCDatalist.wrap(winCCDatalistDto));
        return JsonResponseEntity.success(true);
    }

    public JsonResponseEntity<WinCCDatalistResDto> selectDataListByPage(WinCCDatalistPageQuery dto) {
        Page<WinCCDatalist> page = winCCDatalistService.selectByPage(dto);
        WinCCDatalistResDto resDto = new WinCCDatalistResDto();
        resDto.setResults(WinCCDatalist.covert(page.getRecords()));
//        WinCCDataTotalRowDto totalRowDto = new WinCCDataTotalRowDto();
//        WinCCDatalistDto total = new WinCCDatalistDto();
//        WinCCDatalistDto avg = new WinCCDatalistDto();
//        WinCCDatalistDto max = new WinCCDatalistDto();
//        WinCCDatalistDto min = new WinCCDatalistDto();
//        int size = page.getRecords().size();
//        if (CollectionUtil.isNotBlank(resDto.getResults())) {
//            for (WinCCDatalistDto result : resDto.getResults()) {
//                Field[] fields = WinCCDatalistDto.class.getDeclaredFields();
//                for (Field field : fields) {
//                    field.setAccessible(true);
//                    if (field.getType().isAssignableFrom(Float.class)) {
//                        try {
//                            Float val = (Float) field.get(result);
//                            if (val == null) {
//                                continue;
//                            }
//                            Float totalNum = (Float) field.get(total);
//                            if (totalNum == null) {
//                                totalNum = val;
//                            } else {
//                                totalNum += val;
//                            }
//                            float avgNum = totalNum / size;
//                            Float maxNum = (Float) field.get(max);
//                            if (maxNum == null) {
//                                maxNum = val;
//                            } else {
//                                maxNum = Math.max(maxNum, val);
//                            }
//                            Float minNum = (Float) field.get(min);
//                            if (minNum == null) {
//                                minNum = val;
//                            } else {
//                                minNum = Math.min(minNum, val);
//                            }
//                            field.set(total, totalNum);
//                            field.set(avg, avgNum);
//                            field.set(max, maxNum);
//                            field.set(min, minNum);
//                        } catch (IllegalAccessException ignored) {
//                        }
//                    }
//                    field.setAccessible(false);
//                }
//            }
//        }
//        totalRowDto.setTotal(total);
//        totalRowDto.setMax(max);
//        totalRowDto.setAvg(avg);
//        totalRowDto.setMin(min);
//        resDto.setTotalRow(totalRowDto);
        return JsonResponseEntity.success(resDto, OrmUtils.extraPagination(page));
    }

    public JsonResponseEntity<Boolean> saveDosing(WinCCDosingDevDto dto) {
        winCCDosingDevService.save(WinCCDosingDev.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    public JsonResponseEntity<List<WinCCDosingDevDto>> selectDosingByPage(WinCCDosingPageQuery dto) {
        Page<WinCCDosingDev> page = winCCDosingDevService.selectByPage(dto);
        return JsonResponseEntity.success(WinCCDosingDev.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }


    public JsonResponseEntity<Boolean> saveMag(WinCCMagDevDto dto) {
        winCCMagDevService.save(WinCCMagDev.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    public JsonResponseEntity<List<WinCCMagDevDto>> selectMagByPage(WinCCMagPageQuery dto) {
        Page<WinCCMagDev> page = winCCMagDevService.selectByPage(dto);
        return JsonResponseEntity.success(WinCCMagDev.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    public JsonResponseEntity<Boolean> saveOd11(WinCCOd11DevDto dto) {
        winCCOd11DevService.save(WinCCOd11Dev.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    public JsonResponseEntity<List<WinCCOd11DevDto>> selectOd11ByPage(WinCCOd11PageQuery dto) {
        Page<WinCCOd11Dev> page = winCCOd11DevService.selectByPage(dto);
        return JsonResponseEntity.success(WinCCOd11Dev.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }


    public JsonResponseEntity<Boolean> saveOd12(WinCCOd12DevDto dto) {
        winCCOd12DevService.save(WinCCOd12Dev.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    public JsonResponseEntity<List<WinCCOd12DevDto>> selectOd12ByPage(WinCCOd12PageQuery dto) {
        Page<WinCCOd12Dev> page = winCCOd12DevService.selectByPage(dto);
        return JsonResponseEntity.success(WinCCOd12Dev.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }


    public JsonResponseEntity<Boolean> saveOd20(WinCCOd20DevDto dto) {
        winCCOd20DevService.save(WinCCOd20Dev.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    public JsonResponseEntity<List<WinCCOd20DevDto>> selectOd20ByPage(WinCCOd20PageQuery dto) {
        Page<WinCCOd20Dev> page = winCCOd20DevService.selectByPage(dto);
        return JsonResponseEntity.success(WinCCOd20Dev.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    public JsonResponseEntity<Boolean> saveWps(WinCCWpsDevDto dto) {
        winCCWpsDevService.save(WinCCWpsDev.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    public JsonResponseEntity<List<WinCCWpsDevDto>> selectWpsByPage(WinCCWpsPageQuery dto) {
        Page<WinCCWpsDev> page = winCCWpsDevService.selectByPage(dto);
        return JsonResponseEntity.success(WinCCWpsDev.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }
}
