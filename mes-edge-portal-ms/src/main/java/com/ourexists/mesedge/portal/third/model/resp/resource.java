/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.third.model.resp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class resource {

    /**
     * 制造资源类型：1设备 2刀具 3工装
     */
    private Integer resourcesType;

    /**
     * 制造资源id(设备、刀具、工装等)
     */
    private String resourcesTypeId;

    /**
     * 设备编号
     */
    private String code;

    /**
     * 设备名称
     */
    private String name;

    /**
     * 设备规格
     */
    private String specification;

    /**
     * 工装编号
     */
    private String materialNumber;

    /**
     * 工装名称
     */
    private String materialName;

    /**
     * 工装规格
     */
    private String materialSpecification;

    /**
     * 刀简号
     */
    private String cutternum;

    /**
     * 刀组编码
     */
    private String cutterGroupCode;

    /**
     * 刀柄类型
     */
    private String taperTypeName;

    /**
     * 刀柄通用规格
     */
    private String hiltMark;
}
