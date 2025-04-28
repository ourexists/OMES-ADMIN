/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.tenant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.constants.CommonConstant;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.DateUtil;
import com.ourexists.era.framework.core.utils.tree.TreeUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.ucenter.enums.TenantStatusEnum;
import com.ourexists.mesedge.ucenter.tenant.*;
import com.ourexists.mesedge.ucenter.tenant.mapper.TenantMapper;
import com.ourexists.mesedge.ucenter.tenant.pojo.Tenant;
import com.ourexists.mesedge.ucenter.tenant.service.TenantService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;


/**
 * @author pengcheng
 * @date 2022/4/2 16:20
 * @since 1.0.0
 */
@Service
public class TenantServiceImpl extends AbstractMyBatisPlusService<TenantMapper, Tenant> implements TenantService {

    @Override
    public void settled(TenantSettledDto tenantSettledDto) {
        Tenant tenant = this.getOne(
                new LambdaQueryWrapper<Tenant>()
                        .eq(Tenant::getTenantCode, tenantSettledDto.getTenantCode())
        );
        if (tenant != null) {
            throw new BusinessException("已存在的租户编号!");
        }
        if (tenantSettledDto.getSettledTime() == null) {
            tenantSettledDto.setSettledTime(new Date());
        }
        if (tenantSettledDto.getExpireTime() == null) {
            tenantSettledDto.setExpireTime(DateUtil.getWarpYear(tenantSettledDto.getSettledTime(), 100));
        }

        if (!StringUtils.isEmpty(tenantSettledDto.getPcode())) {
            if (!tenantSettledDto.getTenantCode().startsWith(tenantSettledDto.getPcode())
                    || tenantSettledDto.getTenantCode().length() % 3 != 0
            ) {
                throw new BusinessException("编号规则错误.[父编号+三位新编号构成]");
            }
            List<Tenant> relates = this.list(
                    new LambdaQueryWrapper<Tenant>()
                            .likeRight(Tenant::getTenantCode, tenantSettledDto.getPcode())
            );
            Tenant ptenant = null;
            for (Tenant relate : relates) {
                if (relate.getTenantCode().equals(tenantSettledDto.getPcode())) {
                    ptenant = relate;
                }
            }
            if (ptenant == null) {
                throw new BusinessException("父租户不存在!");
            }
            if (relates.size() - 1 >= ptenant.getManageNum()) {
                throw new BusinessException("超出当前租户可管理的子租户数量");
            }

        }

        //录入租户信息
        this.save(Tenant.warp(tenantSettledDto));
//        if (tenantSettledDto.getType().equals(TenantTypeEnum.CLIENT.name())) {
//            BaseClientDetails baseClientDetails = new BaseClientDetails();
//            BeanUtils.copyProperties(tenantSettledDto.getTenantClientDetails(), baseClientDetails);
//            jdbcClientDetailsService.addClientDetails(baseClientDetails);
//        }
    }

    @Override
    public List<Tenant> tenantToWhichTheUserBelong(String userId) {
        return this.baseMapper.tenantToWhichTheAccBelong(userId, new Date());
    }

    @Override
    public List<Tenant> availableTenantToWhichTheUserBelong(String userId) {
        return this.baseMapper.availableTenantToWhichTheAccBelong(userId, new Date());
    }

    @Override
    public List<TenantUVo> availableTenantRoleWhichTheAccBelong(String accId) {
        return this.baseMapper.availableTenantRoleWhichTheAccBelong(accId, new Date());
    }

    @Override
    public List<TenantUVo> availableTenantToWhichTheUsersBelong(List<String> userIds) {
        return this.baseMapper.availableTenantToWhichTheAccsBelong(userIds, new Date());
    }

    @Override
    public void modifyStatus(String tenantId, TenantStatusEnum tenantStatusEnum) {
        LambdaUpdateWrapper<Tenant> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Tenant::getStatus, tenantStatusEnum.getCode())
                .eq(Tenant::getId, tenantId);
        this.update(wrapper);
    }

    @Override
    public void modify(TenantModifyDto tenantModifyDto) {
        Tenant source = this.getById(tenantModifyDto.getId());
        if (source == null) {
            throw new BusinessException("要修改的对象不存在!");
        }
        if (!tenantModifyDto.getManagement().equals(tenantModifyDto.getManagement())) {
            throw new BusinessException("不允许修改租户管理角色");
        }
        if (tenantModifyDto.getManageNum() != null && tenantModifyDto.getManageNum() < source.getManageNum()) {
            throw new BusinessException("修改的服务商下级租户管理数量不允许小于原先设置！");
        }
        source.setTenantName(tenantModifyDto.getTenantName())
                .setLogo(tenantModifyDto.getLogo())
                .setSettledTime(tenantModifyDto.getSettledTime())
                .setExpireTime(tenantModifyDto.getExpireTime())
                .setAreaFullname(tenantModifyDto.getAreaFullname())
                .setTenantPhone(tenantModifyDto.getTenantPhone())
                .setCityCode(tenantModifyDto.getCityCode())
                .setCountyCode(tenantModifyDto.getCountyCode())
                .setProvinceCode(tenantModifyDto.getProvinceCode())
                .setStreetCode(tenantModifyDto.getStreetCode())
                .setTenantAddress(tenantModifyDto.getTenantAddress())
                .setTenantContacts(tenantModifyDto.getTenantContacts())
                .setTenantCoo(tenantModifyDto.getTenantCoo())
                .setTenantMail(tenantModifyDto.getTenantMail())
                .setTel(tenantModifyDto.getTel())
                .setManagement(tenantModifyDto.getManagement())
                .setManageNum(tenantModifyDto.getManageNum());
        this.updateById(source);
//        if (tenantModifyDto.getType().equals(TenantTypeEnum.CLIENT.name())) {
//            BaseClientDetails baseClientDetails = new BaseClientDetails();
//            BeanUtils.copyProperties(tenantModifyDto.getTenantClientDetails(), baseClientDetails);
//            jdbcClientDetailsService.updateClientDetails(baseClientDetails);
//        }
    }

    @Override
    public Page<Tenant> selectByPage(TenantPageQuery pageQuery) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<Tenant>()
                .eq(!StringUtils.isEmpty(pageQuery.getTenantCode()), Tenant::getTenantCode, pageQuery.getTenantCode())
                .eq(!StringUtils.isEmpty(pageQuery.getPcode()), Tenant::getPcode, pageQuery.getPcode())
                .eq(pageQuery.getManagement() != null, Tenant::getManagement, pageQuery.getManagement())
                .in(CollectionUtil.isNotBlank(pageQuery.getTenantCodes()), Tenant::getTenantCode, pageQuery.getTenantCodes())
                .in(CollectionUtil.isNotBlank(pageQuery.getTenantIds()), Tenant::getTenantCode, pageQuery.getTenantIds())
                .between(pageQuery.getSettledStartTime() != null && pageQuery.getSettledEndTime() != null, Tenant::getSettledTime, pageQuery.getSettledStartTime(), pageQuery.getSettledEndTime())
                .between(pageQuery.getExpireStartTime() != null && pageQuery.getExpireEndTime() != null, Tenant::getExpireTime, pageQuery.getExpireStartTime(), pageQuery.getExpireEndTime())
                .between(pageQuery.getCreatedStartTime() != null && pageQuery.getCreatedEndTime() != null, Tenant::getCreatedTime, pageQuery.getCreatedStartTime(), pageQuery.getCreatedEndTime())
                .like(!StringUtils.isEmpty(pageQuery.getTenantName()), Tenant::getTenantName, pageQuery.getTenantName())
                .like(!StringUtils.isEmpty(pageQuery.getCreatedBy()), Tenant::getCreatedBy, pageQuery.getCreatedBy())
                .orderByDesc(Tenant::getId);
        if (pageQuery.getStatus() != null) {
            statusCondition(wrapper, pageQuery.getStatus());
        }
        return this.page(new Page<>(pageQuery.getPage(), pageQuery.getPageSize()), tenantCondition(wrapper));
    }

    @Override
    public List<Tenant> selectByIds(List<String> tenantIds) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<Tenant>()
                .in(Tenant::getId, tenantIds);
        return this.list(wrapper);
    }

    @Override
    public Tenant selectByCode(String code) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getTenantCode, code);
        return this.getOne(wrapper);
    }


    @Override
    public List<Tenant> selectByCodes(List<String> codes) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<Tenant>()
                .in(Tenant::getTenantCode, codes);
        return this.list(wrapper);
    }

    private LambdaQueryWrapper<Tenant> tenantCondition(LambdaQueryWrapper<Tenant> wrapper) {
        if (!StringUtils.isEmpty(UserContext.getTenant().getTenantId()) &&
                !CommonConstant.SYSTEM_TENANT.equals(UserContext.getTenant().getTenantId())) {
            wrapper.likeRight(Tenant::getTenantCode, UserContext.getTenant().getTenantId());
        }
        return wrapper;
    }

    private void statusCondition(LambdaQueryWrapper<Tenant> wrapper, Integer status) {
        Date current = DateUtil.getCurrentSystemTime();
        if (TenantStatusEnum.COMMON.getCode().equals(status)) {
            wrapper.eq(Tenant::getStatus, TenantStatusEnum.COMMON.getCode());
            wrapper
                    .le(Tenant::getSettledTime, current)
                    .ge(Tenant::getExpireTime, current);
        } else if (TenantStatusEnum.INVALID.getCode().equals(status)) {
            wrapper.eq(Tenant::getStatus, TenantStatusEnum.COMMON.getCode());
            wrapper.and(
                    wrap -> wrap.ge(Tenant::getSettledTime, current)
                            .or()
                            .le(Tenant::getExpireTime, current));

        } else {
            wrapper.eq(Tenant::getStatus, status);
        }
    }

    @Override
    public List<TenantTreeNode> selectTree() {
        List<Tenant> tenantList = this.list(tenantCondition(new LambdaQueryWrapper<>()));
        List<TenantTreeNode> treeNodes = Tenant.covertTree(tenantList);
        return TreeUtil.foldRootTree(treeNodes);
    }

    @Override
    public TenantTreeNode selectParticularTree(String nodeCode) {
        List<Tenant> tenantList = this.list(tenantCondition(
                new LambdaQueryWrapper<Tenant>()
                        .likeRight(Tenant::getTenantCode, nodeCode)));
        List<TenantTreeNode> treeNodes = Tenant.covertTree(tenantList);
        if (CollectionUtil.isBlank(treeNodes)) {
            return null;
        }
        TenantTreeNode pnode = null;
        for (TenantTreeNode treeNode : treeNodes) {
            if (treeNode.getCode().equals(nodeCode)) {
                pnode = treeNode;
            }
        }
        if (pnode == null) {
            return null;
        }
        TreeUtil.mountChildrenNode(pnode, treeNodes);
        return pnode;
    }

    @Override
    public List<Tenant> availableControllableTenantToTheCurrentBelong() {
        return this.list(tenantCondition(new LambdaQueryWrapper<>()));
    }
}
