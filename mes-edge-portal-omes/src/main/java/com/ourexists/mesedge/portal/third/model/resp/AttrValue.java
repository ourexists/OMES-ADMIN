/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.third.model.resp;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AttrValue {

    private String id;

    /**
     * 属性中文名
     */
    private String attrNameCn;

    /**
     * 属性值
     */
    private List<String> attrDefaultValue;

    /**
     * 是否多值属性 1-是 0-否
     */
    private Integer isMultvalues;

    /**
     * 有效值数组
     */
    private String attrEffectiveValue;
}
