/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.constants.CommonConstant;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.user.UserInfo;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.ucenter.account.*;
import com.ourexists.mesedge.ucenter.account.pojo.Account;
import com.ourexists.mesedge.ucenter.account.service.AccountService;
import com.ourexists.mesedge.ucenter.enums.AccRoleEnum;
import com.ourexists.mesedge.ucenter.enums.AccStatusEnum;
import com.ourexists.mesedge.ucenter.feign.AccountFeign;
import com.ourexists.mesedge.ucenter.tenant.TenantUVo;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
//@Tag(name = "账户")
//@RestController
//@RequestMapping("/acc")
@Component
public class AccountViewer implements AccountFeign {

    @Autowired
    private AccountService accountService;

    @Operation(summary = "分页查询")
    @PostMapping("/selectByPage")
    public JsonResponseEntity<List<AccVo>> selectByPage(@RequestBody AccPageQuery pageQuery) {
        if (StringUtils.isEmpty(pageQuery.getPlatform())) {
            pageQuery.setPlatform(UserContext.getPlatForm());
        }
        String currentTenant = UserContext.getTenant().getTenantId();
        if (pageQuery.getQueryChild() != null && pageQuery.getQueryChild() && !currentTenant.equals(CommonConstant.SYSTEM_TENANT)) {
            if (StringUtils.isEmpty(pageQuery.getPTenant()) || !pageQuery.getPTenant().contains(currentTenant)) {
                pageQuery.setPTenant(currentTenant);
            }
        } else {
            pageQuery.setTenantId(UserContext.getTenant().getTenantId());
        }
        Page<AccVo> page = accountService.selectByPage(pageQuery);
        List<AccVo> accVos = page.getRecords();
        if (CollectionUtil.isNotBlank(accVos) && !CommonConstant.SYSTEM_TENANT.equals(currentTenant)) {
            for (AccVo accVo : accVos) {
                if (CollectionUtil.isNotBlank(accVo.getTenantVos())) {
                    List<TenantUVo> newCon = new ArrayList<>();
                    for (TenantUVo tenantVo : accVo.getTenantVos()) {
                        if (tenantVo.getTenantCode().startsWith(currentTenant)) {
                            newCon.add(tenantVo);
                        }
                    }
                    accVo.setTenantVos(newCon);
                }
            }
        }
        return JsonResponseEntity.success(page.getRecords(), OrmUtils.extraPagination(page));
    }

    @Operation(summary = "注册账户")
    @PostMapping("/register")
    public JsonResponseEntity<String> register(@RequestBody @Valid AccRegisterDto accDto) {
        return JsonResponseEntity.success(accountService.register(Account.warp(accDto), accDto.getTenantId(), AccRoleEnum.valueOfName(accDto.getAccRole())));
    }

    @Operation(summary = "账户批入驻")
    @PostMapping("/accBathSettle")
    public JsonResponseEntity<AccVo> accBathSettle(@RequestBody @Valid AccBathSettleDto accDto) {
        Account account = accountService.register(Account.warp(accDto), accDto.getTenantAccDtoList(), accDto.getIsReset());
        return JsonResponseEntity.success(Account.covert(account));
    }

    @Operation(summary = "修改密码")
    @PostMapping("/changePass")
    public JsonResponseEntity<Boolean> changePass(@RequestBody @Valid AccChangePassDto accChangePassDto) {
        accountService.changePass(accChangePassDto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "账户修改")
    @PostMapping("/modify")
    public JsonResponseEntity<Boolean> modify(@RequestBody @Valid AccModifyDto accDto) {
        accountService.modify(accDto);
        return JsonResponseEntity.success(true);
    }


    @Operation(summary = "账户删除(谨慎使用)")
    @PostMapping("/delete")
    public JsonResponseEntity<Boolean> delete(@RequestBody IdsDto idsDto) {
        accountService.removeBatchByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "账户冻结")
    @GetMapping("/frozen")
    public JsonResponseEntity<Boolean> frozen(@RequestParam String accId) {
        accountService.modifyStatus(accId, AccStatusEnum.FROZEN);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "失效用户")
    @GetMapping("/invalid")
    public JsonResponseEntity<Boolean> invalid(@RequestParam String accId) {
        accountService.modifyStatus(accId, AccStatusEnum.INVALID);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "启用用户")
    @GetMapping("/invoke")
    public JsonResponseEntity<Boolean> invoke(@RequestParam String accId) {
        accountService.modifyStatus(accId, AccStatusEnum.COMMON);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "查询部门内的所有账户")
    @GetMapping("/selectByDepart")
    public JsonResponseEntity<List<AccVo>> selectByDepart(@RequestParam String departId) {
        return JsonResponseEntity.success(Account.covert(accountService.selectByDepart(departId)));
    }

    @Operation(summary = "当前用户信息")
    @GetMapping("/currentUser")
    public JsonResponseEntity<UserInfo> currentUser() {
        return JsonResponseEntity.success(UserContext.getUser());
    }

    @Operation(summary = "通过id获取当前用户信息")
    @GetMapping("/selectById")
    public JsonResponseEntity<AccVo> selectById(@RequestParam String id) {
        return JsonResponseEntity.success(Account.covert(accountService.getById(id)));
    }


}
