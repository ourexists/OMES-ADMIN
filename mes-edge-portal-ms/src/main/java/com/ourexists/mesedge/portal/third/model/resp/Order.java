/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.third.model.resp;

import com.alibaba.fastjson.annotation.JSONField;
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

//    /**
//     * 接收时间
//     */
//    @JSONField(format="yyyy-MM-dd HH:mm:ss")
//    private Date deliveryTime;
//
//    /**
//     * 工序序号
//     */
//    private String processNumber;
//
//    /**
//     * 工序名称
//     */
//    private String processName;
//
//    /**
//     * 工艺规程编号
//     */
//    private String processCode;

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

//    /**
//     * 工艺规程名称
//     */
//    private String processSpecificationName;
//
//    /**
//     * 加工方案码
//     */
//    private String processSchemeCode;
//
//    /**
//     * 工艺规程版次
//     */
//    private String processVersion;
//
//    /**
//     * 零件编码
//     */
//    private String partNumber;
//
//    /**
//     * 零件名称
//     */
//    private String partName;
//
//    /**
//     * 工作说明
//     */
//    private String description;
//
//    /**
//     * 毛坯外形尺寸
//     */
//    private String singleSize;
//
//    /**
//     * 成组加工尺寸
//     */
//    private String groupSize;
//
//    /**
//     * 材料编号
//     */
//    private String materialNumber;
//
//    /**
//     * 材料规格
//     */
//    private String materialSpecification;
//
//    /**
//     * 物料编码集合
//     */
//    private List<Plan> planList;
//
//    /**
//     * 工步信息
//     */
//    private List<Step> stepList;
}
