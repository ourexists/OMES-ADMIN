/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.role.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.ucenter.role.RoleDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:13
 * @since 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_ucenter_role")
public class Role extends EraEntity {

    /**
     * 用户组名
     */
    private String name;

    /**
     * 用户组编号
     */
    private String code;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 角色类别
     */
    private Integer type;

    public static Role warp(RoleDto source) {
        Role target = new Role();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static RoleDto covert(Role source) {
        RoleDto target = new RoleDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<RoleDto> covert(List<Role> sources) {
        List<RoleDto> targets = new ArrayList<>();
        if (!CollectionUtils.isEmpty(sources)) {
            for (Role source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }
}
