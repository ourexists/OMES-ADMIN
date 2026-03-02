/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.ucenter.viewer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.omes.ucenter.account.AccPageQuery;
import com.ourexists.omes.ucenter.account.AccVo;
import com.ourexists.omes.ucenter.account.PhonesVo;
import com.ourexists.omes.ucenter.account.pojo.Account;
import com.ourexists.omes.ucenter.account.service.AccountService;
import com.ourexists.omes.ucenter.feign.AccountOpenFeign;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@Tag(name = "会员列表")
//@RestController
//@RequestMapping("/open/acc")
@Component
public class AccountOpenViewer implements AccountOpenFeign {
    @Autowired
    private AccountService accountService;

//    @Operation(summary = "分页查询")
//    @PostMapping("/selectaccListByPage")
    public JsonResponseEntity<List<AccVo>> selectaccListByPage(@RequestBody AccPageQuery pageQuery) {
        Page<AccVo> page = accountService.selectByPage(pageQuery);
        return JsonResponseEntity.success(page.getRecords(), OrmUtils.extraPagination(page));
    }

//    @Operation(summary = "通过手机号获取会员信息")
//    @PostMapping("/selectAccByPhones")
    public JsonResponseEntity<List<AccVo>> selectAccByPhones(@Valid @RequestBody PhonesVo phonesVo) {
        List<Account> accounts = accountService.list(new LambdaQueryWrapper<Account>()
                .in(Account::getMobile, phonesVo.getPhones()));
        return JsonResponseEntity.success(Account.covert(accounts));
    }

//    @Operation(summary = "通过id获取当前用户信息")
//    @GetMapping("/selectById")
    public JsonResponseEntity<AccVo> selectById(@RequestParam String id) {
        return JsonResponseEntity.success(Account.covert(accountService.getById(id)));
    }
}
