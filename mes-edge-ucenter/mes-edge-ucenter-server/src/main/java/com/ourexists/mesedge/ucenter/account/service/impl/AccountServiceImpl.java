/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.DateUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.ucenter.account.*;
import com.ourexists.mesedge.ucenter.account.mapper.AccountMapper;
import com.ourexists.mesedge.ucenter.account.pojo.Account;
import com.ourexists.mesedge.ucenter.account.service.AccountService;
import com.ourexists.mesedge.ucenter.enums.AccRoleEnum;
import com.ourexists.mesedge.ucenter.enums.AccStatusEnum;
import com.ourexists.mesedge.ucenter.permission.pojo.PermissionApi;
import com.ourexists.mesedge.ucenter.permission.service.PermissionService;
import com.ourexists.mesedge.ucenter.tenant.TenantAccDto;
import com.ourexists.mesedge.ucenter.tenant.service.TenantAccService;
import com.ourexists.mesedge.ucenter.tenant.service.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:20
 * @since 1.0.0
 */
@Slf4j
@Service
public class AccountServiceImpl extends AbstractMyBatisPlusService<AccountMapper, Account> implements AccountService {

    @Autowired
    private TenantAccService tenantAccService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissionService permissionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String register(Account account, String belongTenantId, AccRoleEnum accRoleEnum) {
        TenantAccDto tenantAccDto = new TenantAccDto()
                .setTenantId(belongTenantId)
                .setRole(accRoleEnum.name());
        return this.register(account, Collections.singletonList(tenantAccDto), false).getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Account register(Account account, List<TenantAccDto> tenantAccDtoList, boolean isReset) {
        if (StringUtils.isEmpty(account.getPlatform())) {
            account.setPlatform(UserContext.getPlatForm());
        }
        if (account.getSettledTime() == null) {
            account.setSettledTime(new Date());
        }
        if (account.getExpireTime() == null) {
            account.setExpireTime(DateUtil.getWarpDay(account.getSettledTime(), 36600));
        }
        //判断该账户是否已在租户所属平台上注册过
        // 如果注册过的则只需要新增租户的关联关系
        Account other = this.getOne(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getAccName, account.getAccName())
                        .eq(Account::getPlatform, account.getPlatform())
        );
        if (other == null) {
            defaultInfoHandle(account);
            this.save(account);
        } else {
            account = other;
        }

        if (CollectionUtil.isNotBlank(tenantAccDtoList)) {
            for (TenantAccDto tenantAccDto : tenantAccDtoList) {
                if (StringUtils.isEmpty(tenantAccDto.getTenantId())) {
                    tenantAccDto.setTenantId(UserContext.getTenant().getTenantId());
                }
                tenantAccDto.setAccId(account.getId());
            }
            if (isReset) {
                this.tenantAccService.establishRelation(account.getId(), tenantAccDtoList);
            } else {
                this.tenantAccService.addRelation(tenantAccDtoList);
            }
        }
        return account;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modify(AccModifyDto accDto) {
        if (accDto.getId() == null) {
            return;
        }
        Account source = this.getById(accDto.getId());
        if (source == null) {
            throw new BusinessException("请选择要修改的账户！");
        }
        //判断手机号是否被平台其它账户占用
        if (!StringUtils.isEmpty(accDto.getMobile())
                && !accDto.getMobile().equals(source.getMobile())) {
            Account other = this.getOne(
                    new LambdaQueryWrapper<Account>()
                            .eq(Account::getMobile, accDto.getMobile())
                            .eq(Account::getPlatform, source.getPlatform())
            );
            if (other != null) {
                throw new BusinessException("手机号已被使用!");
            }
        }
        BeanUtils.copyProperties(accDto, source);
        this.updateById(source);
    }

    /**
     * 账户默认参数处理
     *
     * @param account
     */
    private void defaultInfoHandle(Account account) {
        if (StringUtils.isEmpty(account.getPassword())) {
            account.setInit(true);
            account.setPassword("");
        } else {
            account.setPassword(passwordEncoder.encode(account.getPassword()));
        }
        if (account.getSettledTime() == null) {
            account.setSettledTime(new Date());
        }
        if (account.getExpireTime() == null) {
            account.setExpireTime(DateUtil.getWarpYear(account.getSettledTime(), 100));
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyStatus(String accId, AccStatusEnum accStatusEnum) {
        LambdaUpdateWrapper<Account> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Account::getStatus, accStatusEnum.getCode())
                .eq(Account::getId, accId);
        this.update(wrapper);
    }

    @Override
    public Page<AccVo> selectByPage(AccPageQuery pageQuery) {
        Page<AccVo> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
        int count = this.baseMapper.pageCount(pageQuery);
        page.setTotal(count);
        List<AccVo> accVos = new ArrayList<>();
        if (count > 0) {
            List<Account> contents = this.baseMapper.page(pageQuery);
            for (Account content : contents) {
                AccVo accVo = Account.covert(content);
                if (pageQuery.getQueryTenant()) {
                    accVo.setTenantVos(tenantService.availableTenantRoleWhichTheAccBelong(content.getId()));
                }
                accVos.add(accVo);
            }
        }
        page.setRecords(accVos);
        return page;
    }

    @Override
    public Account selectByAccName(String accName, String platform) {
        return this.getOne(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getAccName, accName)
                        .eq(Account::getPlatform, platform)
        );
    }

    @Override
    public Account selectByMobile(String mobile, String platform) {
        return this.getOne(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getMobile, mobile)
                        .eq(Account::getPlatform, platform)
                        .orderByAsc(Account::getId)
                        .last("limit 1")
        );
    }

    @Override
    public List<Account> selectByDepart(String departId) {
        return this.baseMapper.selectByDepart(departId);
    }


    @Override
    public void changePass(AccChangePassDto accChangePassDto) {
        Account account = this.selectByAccName(accChangePassDto.getAccName(), accChangePassDto.getPlatform());
        if (account == null) {
            throw new BusinessException("账户不存在！");
        }
        if (!this.passwordEncoder.matches(accChangePassDto.getOldPass(), account.getPassword())) {
            throw new BusinessException("旧密码不正确！");
        }
        if (this.passwordEncoder.matches(accChangePassDto.getNewPass(), account.getPassword())) {
            throw new BusinessException("新旧密码不能一致！");
        }
        if (!accChangePassDto.getNewPass().equals(accChangePassDto.getConfirmPass())) {
            throw new BusinessException("两次密码不一致！");
        }
        account.setPassword(this.passwordEncoder.encode(accChangePassDto.getNewPass()));
        this.updateById(account);
    }

    @Override
    public boolean resetPassByMobile(AccResetPassPhoneDto resetPassPhoneDto) {
        Account account =  this.selectByMobile(resetPassPhoneDto.getMobile(), resetPassPhoneDto.getPlatform());
        if (account == null) {
            throw new BusinessException("账户不存在！");
        }
        account.setPassword(this.passwordEncoder.encode(resetPassPhoneDto.getNewPass()));
        return this.updateById(account);
    }

    @Override
    public boolean checkPass(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public Account associateByMobile(String mobile, String source, String sourceId, Boolean force, String platform) {
        Account account = this.selectByMobile(mobile, platform);
        if (account == null) {
            return null;
        }
        if (!force && !StringUtils.isEmpty(account.getSource()) && !StringUtils.isEmpty(account.getSourceId())) {
            return null;
        }
        account.setSource(source);
        account.setSourceId(sourceId);
        this.updateById(account);
        return account;
    }


    @Override
    public Account getByAccName(String accName) {
        return this.getOne(new LambdaQueryWrapper<Account>().eq(Account::getAccName, accName));
    }

    public AccVo extraInfo(AccVo accVo) {
        accVo.setTenantVos(tenantService.availableTenantRoleWhichTheAccBelong(accVo.getId()));
        accVo.setPermissionApiDetailDtos(PermissionApi.covert(permissionService.selectAccPermissionApiGroupByTenant(accVo.getId())));
        return accVo;
    }
}
