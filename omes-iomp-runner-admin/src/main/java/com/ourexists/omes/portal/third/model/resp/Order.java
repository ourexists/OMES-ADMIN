/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.third.model.resp;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Order {

    private String id;

    /**
     * 物料编号
     */
    private List<String> barCodeList;

    /**
     * 工艺路线
     */
    private String processRoute;

    private Integer priority;

    /**
     * 工号
     */
    private String planFrameCode;

    /**
     * 计划开始时间
     */
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date planStartTime;

    /**
     * 计划结束时间
     */
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date planEndTime;
}
