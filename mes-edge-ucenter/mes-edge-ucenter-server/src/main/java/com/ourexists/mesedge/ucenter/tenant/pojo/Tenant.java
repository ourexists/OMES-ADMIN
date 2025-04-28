/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.tenant.pojo;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.constants.ManagementControlEnum;
import com.ourexists.era.framework.core.utils.DateUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.ucenter.enums.TenantStatusEnum;
import com.ourexists.mesedge.ucenter.tenant.TenantModifyDto;
import com.ourexists.mesedge.ucenter.tenant.TenantSettledDto;
import com.ourexists.mesedge.ucenter.tenant.TenantTreeNode;
import com.ourexists.mesedge.ucenter.tenant.TenantVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:13
 * @since 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("p_ucenter_tenant")
public class Tenant extends EraEntity {

    /**
     * 租戶名
     */
    private String tenantName;

    /**
     * 租户编号
     */
    private String tenantCode;

    /**
     * 租户logo
     */
    private String logo;

    /**
     * 父租户编号
     */
    private String pcode;

    /**
     * 租户状态
     */
    private Integer status;

    /**
     * 租戶入驻时间
     */
    private Date settledTime;

    /**
     * 租户到期时间
     */
    private Date expireTime;

    /**
     * 此处采用逻辑删除
     */
    @TableLogic
    private Integer delFlag;

    private String clientId;

    /**
     * 租户/机构所在地址
     */
    private String tenantAddress;

    /**
     * 联系人姓名
     */
    private String tenantContacts;

    /**
     * 服务电话
     */
    private String tenantPhone;

    /**
     * 客服电话
     */
    private String tel;

    /**
     * 联系邮箱
     */
    private String tenantMail;
    /**
     * 省份code
     */
    private String provinceCode;

    /**
     * 城市code
     */
    private String cityCode;

    /**
     * 区县code
     */
    private String countyCode;

    /**
     * 街道code
     */
    private String streetCode;

    /**
     * 所在区域：省-市-区县-街道拼接
     */
    private String areaFullname;

    /**
     * 租户机构经纬度坐标
     */
    private String tenantCoo;

    /**
     * 管控权
     */
    private Integer management = ManagementControlEnum.ALONE.getCode();

    /**
     * 管控数量
     */
    private Integer manageNum = 0;


    public static Tenant warp(TenantSettledDto source) {
        Tenant target = new Tenant();
        BeanUtils.copyProperties(source, target);
        target.setStatus(TenantStatusEnum.COMMON.getCode());
        if (target.getExpireTime() == null) {
            target.setExpireTime(new Date());
        }
        return target;
    }


    public static Tenant warp(TenantModifyDto source) {
        Tenant target = new Tenant();
        BeanUtils.copyProperties(source, target);
        target.setStatus(TenantStatusEnum.COMMON.getCode());
        if (target.getExpireTime() == null) {
            target.setExpireTime(new Date());
        }
        return target;
    }

    public static TenantVo covert(Tenant source) {
        TenantVo target = new TenantVo();
        BeanUtils.copyProperties(source, target);
        target.setStatusDesc(TenantStatusEnum.valueof(source.getStatus()).getDesc());
        return target;
    }

    public static List<TenantVo> covert(List<Tenant> sources) {
        List<TenantVo> targets = new ArrayList<>();
        if (!CollectionUtils.isEmpty(sources)) {
            for (Tenant source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static TenantTreeNode covertTree(Tenant source) {
        TenantTreeNode target = new TenantTreeNode();
        BeanUtils.copyProperties(source, target);
        target.setCode(source.getTenantCode());
        target.setStatusDesc(TenantStatusEnum.valueof(source.getStatus()).getDesc());
        return target;
    }


    public static List<TenantTreeNode> covertTree(List<Tenant> sources) {
        List<TenantTreeNode> targets = new ArrayList<>();
        if (!CollectionUtils.isEmpty(sources)) {
            for (Tenant source : sources) {
                targets.add(covertTree(source));
            }
        }
        return targets;
    }

    public Integer getStatus() {
        Date current = DateUtil.getCurrentSystemTime();
        if (this.status.equals(TenantStatusEnum.COMMON.getCode())) {
            if (this.settledTime != null && this.settledTime.getTime() > current.getTime()) {
                return TenantStatusEnum.INVALID.getCode();
            }
            if (this.expireTime != null && this.expireTime.getTime() < current.getTime()) {
                return TenantStatusEnum.INVALID.getCode();
            }
        }
        return status;
    }
}
