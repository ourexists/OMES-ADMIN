/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.account.AccPageQuery;
import com.ourexists.mesedge.ucenter.account.AccVo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author pengcheng
 * @version 1.0.0
 * @date 2023/4/7 11:11
 * @since 1.0.0
 */
//@Tag(name = "账户全量数据")
//@RestController
//@RequestMapping("/overall/acc")
public interface AccountOverallFeign {

//    @Operation(summary = "通过账户名获取当前用户信息")
//    @GetMapping("/selectByAccName")
    JsonResponseEntity<AccVo> selectByAccName(@RequestParam String accName);

//    @Operation(summary = "分页查询")
//    @PostMapping("/selectByPage")
    JsonResponseEntity<List<AccVo>> selectByPage(@RequestBody AccPageQuery pageQuery);

}
