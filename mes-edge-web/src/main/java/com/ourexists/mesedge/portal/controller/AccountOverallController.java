/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.account.AccPageQuery;
import com.ourexists.mesedge.ucenter.account.AccVo;
import com.ourexists.mesedge.ucenter.feign.AccountOverallFeign;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author pengcheng
 * @version 1.0.0
 * @date 2023/4/7 11:11
 * @since 1.0.0
 */
@Tag(name = "账户全量数据")
@RestController
@RequestMapping("/overall/acc")
public class AccountOverallController {

    @Autowired
    private AccountOverallFeign accountOverallFeign;

    @Operation(summary = "通过账户名获取当前用户信息")
    @GetMapping("/selectByAccName")
    public JsonResponseEntity<AccVo> selectByAccName(@RequestParam String accName) {
        return accountOverallFeign.selectByAccName(accName);
    }

    @Operation(summary = "分页查询")
    @PostMapping("/selectByPage")
    public JsonResponseEntity<List<AccVo>> selectByPage(@RequestBody AccPageQuery pageQuery) {
        return accountOverallFeign.selectByPage(pageQuery);
    }

}
