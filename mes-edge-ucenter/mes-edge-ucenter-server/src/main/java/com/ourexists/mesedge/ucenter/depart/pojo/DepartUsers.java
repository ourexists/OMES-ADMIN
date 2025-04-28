/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.depart.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author pengcheng
 * @date 2022/4/6 16:13
 * @since 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("r_ucenter_depart_users")
public class DepartUsers {

    /**
     * 部门id
     */
    private String departId;

    /**
     * 账户id
     */
    private String accId;
}
