/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.ucenter.account.AccPageQuery;
import com.ourexists.mesedge.ucenter.account.AccVo;
import com.ourexists.mesedge.ucenter.account.pojo.Account;
import com.ourexists.mesedge.ucenter.account.service.AccountService;
import com.ourexists.mesedge.ucenter.feign.AccountOverallFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
@Component
public class AccountOverallViewer implements AccountOverallFeign {

    @Autowired
    private AccountService accountService;

    //    @Operation(summary = "通过账户名获取当前用户信息")
//    @GetMapping("/selectByAccName")
    public JsonResponseEntity<AccVo> selectByAccName(@RequestParam String accName) {
        return JsonResponseEntity.success(Account.covert(accountService.getByAccName(accName)));
    }

    //    @Operation(summary = "分页查询")
//    @PostMapping("/selectByPage")
    public JsonResponseEntity<List<AccVo>> selectByPage(@RequestBody AccPageQuery pageQuery) {
        Page<AccVo> page = accountService.selectByPage(pageQuery);
        return JsonResponseEntity.success(page.getRecords(), OrmUtils.extraPagination(page));
    }

}
