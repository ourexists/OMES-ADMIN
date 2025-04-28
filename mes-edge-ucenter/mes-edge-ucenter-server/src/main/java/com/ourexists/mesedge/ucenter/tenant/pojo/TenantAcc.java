/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.tenant.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.mesedge.ucenter.enums.AccRoleEnum;
import com.ourexists.mesedge.ucenter.tenant.TenantAccDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/6 16:13
 * @since 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("r_ucenter_tenant_acc")
public class TenantAcc {

    private String tenantId;

    private String accId;

    private String role;

    public static TenantAcc warp(TenantAccDto source) {
        TenantAcc target = new TenantAcc();
        if (StringUtils.isEmpty(source.getRole())) {
            source.setRole(AccRoleEnum.COMMON.name());
        }
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<TenantAcc> warp(List<TenantAccDto> sources) {
        List<TenantAcc> targets = new ArrayList<>();
        if (CollectionUtil.isBlank(sources)) {
            return targets;
        }
        for (TenantAccDto source : sources) {
            targets.add(warp(source));
        }
        return targets;
    }


    public static List<TenantAcc> warp(String accId, List<TenantAccDto> sources) {
        List<TenantAcc> targets = new ArrayList<>();
        if (CollectionUtil.isBlank(sources)) {
            return targets;
        }
        for (TenantAccDto source : sources) {
            source.setAccId(accId);
            targets.add(warp(source));
        }
        return targets;
    }
}
