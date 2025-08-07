/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.account.AccPageQuery;
import com.ourexists.mesedge.ucenter.account.AccVo;
import com.ourexists.mesedge.ucenter.account.PhonesVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@Tag(name = "会员列表")
//@RestController
//@RequestMapping("/open/acc")
public interface AccountOpenFeign {

    //    @Operation(summary = "分页查询")
//    @PostMapping("/selectaccListByPage")
    JsonResponseEntity<List<AccVo>> selectaccListByPage(@RequestBody AccPageQuery pageQuery);

    //    @Operation(summary = "通过手机号获取会员信息")
//    @PostMapping("/selectAccByPhones")
    JsonResponseEntity<List<AccVo>> selectAccByPhones(@Valid @RequestBody PhonesVo phonesVo);

    //    @Operation(summary = "通过id获取当前用户信息")
//    @GetMapping("/selectById")
    JsonResponseEntity<AccVo> selectById(@RequestParam String id);
}
