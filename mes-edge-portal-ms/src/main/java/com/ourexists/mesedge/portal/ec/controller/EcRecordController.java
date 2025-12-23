/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.ec.controller;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.DateUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.ec.feign.EcRecordFeign;
import com.ourexists.mesedge.ec.model.EcRecordDto;
import com.ourexists.mesedge.ec.model.EcRecordQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Tag(name = "能耗记录")
@RestController
@RequestMapping("/ec_record")
public class EcRecordController {

    @Autowired
    private EcRecordFeign feign;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByCondition")
    public JsonResponseEntity<List<Map<String, String>>> selectByCondition(@RequestBody EcRecordQuery dto) {
        try {
            List<Map<String, String>> rr = new ArrayList<>();
            List<EcRecordDto> r = RemoteHandleUtils.getDataFormResponse(feign.selectByCondition(dto));
            if (CollectionUtils.isEmpty(r)) {
                return JsonResponseEntity.success(new ArrayList<>());
            }
            Map<String, List<EcRecordDto>> map = new HashMap<>();
            for (EcRecordDto ecRecordDto : r) {
                List<EcRecordDto> tt = map.get(ecRecordDto.getRecordId());
                if (CollectionUtils.isEmpty(tt)) {
                    tt = new ArrayList<>();
                }
                tt.add(ecRecordDto);
                map.put(ecRecordDto.getRecordId(), tt);
            }
            for (Map.Entry<String, List<EcRecordDto>> entry : map.entrySet()) {
                Map<String, String> mm = new HashMap<>();
                for (EcRecordDto ecRecordDto : entry.getValue()) {
                    mm.put("time", DateUtil.dateTimeFormat(ecRecordDto.getTime()));
                    mm.put(ecRecordDto.getAttrId(), ecRecordDto.getAttrVal());
                }
                rr.add(mm);
            }
            rr.sort(Comparator.comparing((Map<String, String> m) -> {
                try {
                    String time = m.get("time");
                    if (time == null || time.isBlank()) {
                        return null;
                    }
                    return DateUtil.dateTimeParser(time).getTime();
                } catch (Exception e) {
                    return null;
                }
            }, Comparator.nullsLast(Long::compareTo)).reversed());
            return JsonResponseEntity.success(rr);
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Operation(summary = "批量新增", description = "批量新增")
    @PostMapping("addBatch")
    public JsonResponseEntity<Boolean> addBatch(@Validated @RequestBody List<List<EcRecordDto>> dto) {
        return feign.addBatch(dto);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }
}
