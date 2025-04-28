/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.role.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author pengcheng
 * @date 2022/4/12 17:16
 * @since 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("r_ucenter_acc_role")
public class AccRole {

    private String accId;

    private String roleId;
}
