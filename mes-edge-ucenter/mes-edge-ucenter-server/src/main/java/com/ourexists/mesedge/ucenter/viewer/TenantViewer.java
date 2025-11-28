/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.viewer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.constants.CommonConstant;
import com.ourexists.era.framework.core.constants.ManagementControlEnum;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.TenantInfo;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.era.oauth2.core.OAuth2Role;
import com.ourexists.mesedge.ucenter.account.AccVo;
import com.ourexists.mesedge.ucenter.account.pojo.Account;
import com.ourexists.mesedge.ucenter.account.service.AccountService;
import com.ourexists.mesedge.ucenter.enums.AccRoleEnum;
import com.ourexists.mesedge.ucenter.enums.TenantStatusEnum;
import com.ourexists.mesedge.ucenter.feign.TenantFeign;
import com.ourexists.mesedge.ucenter.tenant.*;
import com.ourexists.mesedge.ucenter.tenant.pojo.Tenant;
import com.ourexists.mesedge.ucenter.tenant.service.TenantAccService;
import com.ourexists.mesedge.ucenter.tenant.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
@Slf4j
//@Tag(name = "租户")
//@RestController
//@RequestMapping("/tenant")
@Component
public class TenantViewer implements TenantFeign {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TenantAccService tenantAccService;

    @Operation(summary = "分页查询")
    @PostMapping("/selectByPage")
    public JsonResponseEntity<List<TenantVo>> selectByPage(@RequestBody TenantPageQuery tenantPageQuery) {
        Page<Tenant> page = tenantService.selectByPage(tenantPageQuery);
        return JsonResponseEntity.success(Tenant.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    @Operation(summary = "查询租户信息")
    @GetMapping("/selectList")
    public JsonResponseEntity<List<TenantVo>> selectList(@RequestParam List<String> tenantIds) {
        return JsonResponseEntity.success(Tenant.covert(tenantService.selectByIds(tenantIds)));
    }

    @Operation(summary = "查询租户树")
    @GetMapping("/selectTree")
    public JsonResponseEntity<List<TenantTreeNode>> selectTree() {
        return JsonResponseEntity.success(tenantService.selectTree());
    }

    @Operation(summary = "查询特定节点的租户树")
    @GetMapping("/selectParticularTree")
    public JsonResponseEntity<TenantTreeNode> selectParticularTree(@RequestParam String nodeCode) {
        return JsonResponseEntity.success(tenantService.selectParticularTree(nodeCode));
    }

    @Operation(summary = "租户入驻")
    @PostMapping("/settled")
    public JsonResponseEntity<Boolean> settled(@RequestBody @Valid TenantSettledDto tenantSettledDto) {
        operaAuthCheck();
        tenantService.settled(tenantSettledDto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "租户信息修改")
    @PostMapping("/modify")
    public JsonResponseEntity<Boolean> modify(@RequestBody @Valid TenantModifyDto tenantModifyDto) {
        operaAuthCheck();
        tenantService.modify(tenantModifyDto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "租户冻结")
    @GetMapping("/frozen")
    public JsonResponseEntity<Boolean> frozen(@RequestParam String tenantId) {
        operaAuthCheck();
        tenantService.modifyStatus(tenantId, TenantStatusEnum.FROZEN);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "租户解冻")
    @GetMapping("/unfrozen")
    public JsonResponseEntity<Boolean> unfrozen(@RequestParam String tenantId) {
        operaAuthCheck();
        tenantService.modifyStatus(tenantId, TenantStatusEnum.COMMON);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "租户删除")
    @PostMapping("/delete")
    public JsonResponseEntity<Boolean> delete(@RequestBody IdsDto idsDto) {
        operaAuthCheck();
        tenantService.removeBatchByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "登录用户归属的租户")
    @GetMapping("/tenantToWhichTheCurrentUserBelong")
    public JsonResponseEntity<List<TenantNodeVo>> tenantToWhichTheCurrentUserBelong() {
        List<TenantVo> tenantVos = Tenant.covert(tenantService.availableTenantToWhichTheUserBelong(UserContext.getUser().getId()));
        List<TenantNodeVo> r = new ArrayList<>();
        if (CollectionUtil.isNotBlank(tenantVos)) {
            for (TenantVo tenantVo : tenantVos) {
                TenantNodeVo t = new TenantNodeVo();
                BeanUtils.copyProperties(tenantVo, t);
                if (StringUtils.isNotBlank(t.getPcode()) && !t.getPcode().equals(CommonConstant.SYSTEM_TENANT)) {
                    t.setPTenant(Tenant.covert(tenantService.selectByCode(tenantVo.getPcode())));
                }
                r.add(t);
            }
        }
        return JsonResponseEntity.success(r);
    }

    @Operation(summary = "用户归属的租户")
    @GetMapping("/tenantToWhichTheUserBelong")
    public JsonResponseEntity<List<TenantVo>> tenantToWhichTheUserBelong(@RequestParam String userId) {
        return JsonResponseEntity.success(Tenant.covert(tenantService.tenantToWhichTheUserBelong(userId)));
    }

    @Operation(summary = "用户归属的可用租户")
    @GetMapping("/availableTenantToWhichTheUserBelong")
    public JsonResponseEntity<List<TenantNodeVo>> availableTenantToWhichTheUserBelong(@RequestParam String userId) {
        List<TenantVo> tenantVos = Tenant.covert(tenantService.availableTenantToWhichTheUserBelong(userId));
        List<TenantNodeVo> r = new ArrayList<>();
        if (CollectionUtil.isNotBlank(tenantVos)) {
            for (TenantVo tenantVo : tenantVos) {
                TenantNodeVo t = new TenantNodeVo();
                BeanUtils.copyProperties(tenantVo, t);
                log.info("=============上级租户==========");
                if (StringUtils.isNotBlank(t.getPcode()) && !t.getPcode().equals(CommonConstant.SYSTEM_TENANT)) {
                    t.setPTenant(Tenant.covert(tenantService.selectByCode(tenantVo.getPcode())));
                }
                r.add(t);
            }
        }
        return JsonResponseEntity.success(r);
    }

    @Operation(summary = "用户归属的可用租户")
    @GetMapping("/availableTenantToWhichTheUsersBelong")
    public JsonResponseEntity<List<TenantUVo>> availableTenantToWhichTheUsersBelong(@RequestParam List<String> userIds) {
        return JsonResponseEntity.success(tenantService.availableTenantToWhichTheUsersBelong(userIds));
    }

    @Operation(summary = "挂载账户")
    @PostMapping("/mountAcc")
    public JsonResponseEntity<Boolean> mountAcc(@Valid @RequestBody TenantMountDto tenantMountDto) {
        tenantAccService.addRelation(tenantMountDto.getAccId(), tenantMountDto.getTenantId(), AccRoleEnum.valueOfName(tenantMountDto.getAccRole()));
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "解除挂载账户")
    @PostMapping("/relieveMountAcc")
    public JsonResponseEntity<Boolean> relieveMountAcc(@Valid @RequestBody TenantMountDto tenantMountDto) {
        tenantAccService.removeRelation(tenantMountDto.getAccId(), tenantMountDto.getTenantId());
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "查询当前租户可管控的所有租户")
    @GetMapping("/availableControllableTenantToTheCurrentBelong")
    public JsonResponseEntity<List<TenantVo>> availableControllableTenantToTheCurrentBelong() {
        return JsonResponseEntity.success(Tenant.covert(tenantService.availableControllableTenantToTheCurrentBelong()));
    }

    @Operation(summary = "根据tenantId或者tenantCode（非主键id）批量查询租户信息")
    @GetMapping("/selectListByTenantCode")
    public JsonResponseEntity<List<TenantVo>> selectListByTenantCode(@RequestParam("tenantIds") List<String> tenantIds) {
        UserContext.defaultTenant();
        List<Tenant> res = tenantService.list(new LambdaQueryWrapper<Tenant>().in(Tenant::getTenantCode, tenantIds));
        return JsonResponseEntity.success(Tenant.covert(res));
    }


    @Operation(summary = "通过手机号挂载")
    @PostMapping("/mountByPhones")
    public JsonResponseEntity<List<AccVo>> mountByPhones(@Valid @RequestBody TenantSettlePhones tenantSettlePhones) {
        if (StringUtils.isEmpty(tenantSettlePhones.getRole())) {
            tenantSettlePhones.setRole(OAuth2Role.COMMON.name());
        }
        List<Account> accounts = accountService.list(new LambdaQueryWrapper<Account>().eq(Account::getPlatform, tenantSettlePhones.getPlatform()).in(Account::getMobile, tenantSettlePhones.getPhones()));
        List<String> accIds = accounts.stream().map(Account::getId).collect(Collectors.toList());
        tenantAccService.incrementMount(tenantSettlePhones.getTenantCode(), AccRoleEnum.valueOf(tenantSettlePhones.getRole()), accIds);
        return JsonResponseEntity.success(Account.covert(accounts));
    }

    @Operation(summary = "通过身份证号挂载")
    @PostMapping("/mountByIdCards")
    public JsonResponseEntity<List<AccVo>> mountByIdCards(@Valid @RequestBody TenantSettleIdCards tenantSettleIdCards) {
        if (StringUtils.isEmpty(tenantSettleIdCards.getRole())) {
            tenantSettleIdCards.setRole(AccRoleEnum.COMMON.name());
        }
        List<Account> accounts = accountService.list(new LambdaQueryWrapper<Account>().eq(Account::getPlatform, tenantSettleIdCards.getPlatform()).in(Account::getIdCard, tenantSettleIdCards.getIdCards()));
        List<String> accIds = accounts.stream().map(Account::getId).collect(Collectors.toList());
        tenantAccService.incrementMount(tenantSettleIdCards.getTenantCode(), AccRoleEnum.valueOf(tenantSettleIdCards.getRole()), accIds);
        return JsonResponseEntity.success(Account.covert(accounts));
    }

    @GetMapping("/all")
    @Operation(summary = "所有租户信息")
    public JsonResponseEntity<List<TenantVo>> all() {
        return JsonResponseEntity.success(Tenant.covert(tenantService.list()));
    }

    private void operaAuthCheck() {
        TenantInfo tenantInfo = UserContext.getTenant();
        if (tenantInfo != null && !CommonConstant.SYSTEM_TENANT.equals(tenantInfo.getTenantId()) && ManagementControlEnum.ALONE.getCode().equals(tenantInfo.getManagementControl())) {
            throw new BusinessException("该租户暂不支持新增站点，请联系销售顾问!");
        }
    }
}
