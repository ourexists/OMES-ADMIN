/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.report.model.FzDataDto;
import com.ourexists.mesedge.report.model.FzDataPageQuery;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//@RequestMapping("/task")
public interface FzDataFeign {

    JsonResponseEntity<List<FzDataDto>> selectByPage(@RequestBody FzDataPageQuery dto);

    JsonResponseEntity<List<String>> allPFName();
}
