/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.account.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.ucenter.account.*;
import com.ourexists.mesedge.ucenter.account.pojo.Account;
import com.ourexists.mesedge.ucenter.enums.AccRoleEnum;
import com.ourexists.mesedge.ucenter.enums.AccStatusEnum;
import com.ourexists.mesedge.ucenter.tenant.TenantAccDto;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface AccountService extends IMyBatisPlusService<Account> {

    /**
     * 注册账户
     *
     * @param account      账户信息
     * @param belongTenant 归属的租户
     * @param accRoleEnum      租户中的大角色
     */
    String register(Account account, String belongTenant, AccRoleEnum accRoleEnum);


    /**
     * 注册账户并批量入驻租户
     *
     * @param account
     * @param tenantAccDtoList
     * @param isReset          是否重置租户关系
     * @return
     */
    Account register(Account account, List<TenantAccDto> tenantAccDtoList, boolean isReset);

    /**
     * 修改账户信息
     * <p>
     * 只支持账户信息的修改，不支持修改租户关联
     *
     * @param accDto
     */
    void modify(AccModifyDto accDto);

    /**
     * 修改用户状态
     *
     * @param accId         账户id
     * @param accStatusEnum 账户状态
     */
    void modifyStatus(String accId, AccStatusEnum accStatusEnum);

    /**
     * 分页查询
     *
     * @param pageQuery 分页条件
     * @return 分页数据
     */
    Page<AccVo> selectByPage(AccPageQuery pageQuery);

    /**
     * 通过账户名查询平台下的账户
     *
     * @param accName  账户名
     * @param platform 所属平台
     * @return
     */
    Account selectByAccName(String accName, String platform);

    /**
     * 通过手机号查询平台下的账户
     *
     * @param mobile   手机号
     * @param platform 所属平台
     * @return
     */
    Account selectByMobile(String mobile, String platform);

    /**
     * 查询部门内所有的用户
     *
     * @param departId 部门id
     * @return
     */
    List<Account> selectByDepart(String departId);


    /**
     * 修改密码
     *
     * @param accChangePassDto
     */
    void changePass(AccChangePassDto accChangePassDto);

    boolean resetPassByMobile(AccResetPassPhoneDto resetPassPhoneDto);


    boolean checkPass(String rawPassword, String encodedPassword);

    Account associateByMobile(String mobile, String source, String sourceId, Boolean force, String platform);

    Account getByAccName(String accName);

    AccVo extraInfo(AccVo accVo);
}
