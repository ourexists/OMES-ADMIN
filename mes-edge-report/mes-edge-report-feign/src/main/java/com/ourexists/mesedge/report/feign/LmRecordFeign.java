/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.report.model.LmRecordDto;
import com.ourexists.mesedge.report.model.LmRecordPageQuery;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//@RequestMapping("/task")
public interface LmRecordFeign {

    JsonResponseEntity<List<LmRecordDto>> selectByPage(@RequestBody LmRecordPageQuery dto);
}
