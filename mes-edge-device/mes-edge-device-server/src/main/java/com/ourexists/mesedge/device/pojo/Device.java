/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.pojo;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.device.model.DeviceDto;
import com.ourexists.mesedge.device.model.DeviceTreeNode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 配方分类
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_device")
public class Device extends MainEntity {

    private String selfCode;

    //级联编号
    private String code;

    private String pcode;

    private String name;

    private String dgId;

    private Integer type;

    private Integer localization;

    private BigDecimal maxCapacity;

    private BigDecimal availableCapacity;

    private Integer status;

    private String matCode;

    @TableLogic
    private Boolean delBit;

    public static DeviceTreeNode covert(Device source) {
        if (source == null) {
            return null;
        }
        DeviceTreeNode target = new DeviceTreeNode();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<DeviceTreeNode> covert(List<Device> sources) {
        List<DeviceTreeNode> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static Device wrap(DeviceDto source) {
        Device target = new Device();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<Device> wrap(List<DeviceDto> sources) {
        List<Device> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
