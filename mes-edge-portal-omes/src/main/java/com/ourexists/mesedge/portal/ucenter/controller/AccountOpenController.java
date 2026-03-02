/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.ucenter.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.account.AccPageQuery;
import com.ourexists.mesedge.ucenter.account.AccVo;
import com.ourexists.mesedge.ucenter.account.PhonesVo;
import com.ourexists.mesedge.ucenter.feign.AccountOpenFeign;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "会员列表")
@RestController
@RequestMapping("/open/acc")
public class AccountOpenController {
    @Autowired
    private AccountOpenFeign accountOpenFeign;

    @Operation(summary = "分页查询")
    @PostMapping("/selectaccListByPage")
    public JsonResponseEntity<List<AccVo>> selectaccListByPage(@RequestBody AccPageQuery pageQuery) {
        return accountOpenFeign.selectaccListByPage(pageQuery);
    }

    @Operation(summary = "通过手机号获取会员信息")
    @PostMapping("/selectAccByPhones")
    public JsonResponseEntity<List<AccVo>> selectAccByPhones(@Valid @RequestBody PhonesVo phonesVo) {
        return accountOpenFeign.selectAccByPhones(phonesVo);
    }

    @Operation(summary = "通过id获取当前用户信息")
    @GetMapping("/selectById")
    public JsonResponseEntity<AccVo> selectById(@RequestParam String id) {
        return accountOpenFeign.selectById(id);
    }
}
