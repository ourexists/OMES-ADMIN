/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.third.model.resp;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Step {

    private String id;

    /**
     * 工步序号
     */
    private String stepNum;

    /**
     * 工步名称
     */
    private String stepName;

    /**
     * 工步关重属性
     */
    private String stepProperty;

    /**
     * 工作说明
     */
    private String description;

    /**
     * 工时(min)
     */
    private Integer processingTime;

    /**
     * 工步类型
     */
    private Integer categoryId;

    /**
     * 工序制造资源信息
     */
    private List<resource> resourceList;

    /**
     * 工步程序信息
     */
    private List<Nc> ncList;

    /**
     * 工步自定义属性值
     */
    private List<AttrValue> attrValueList;
}
