/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.sync.model.DeviceCollectBindingDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备采集绑定：将某连接下某数据源（topic/nodeId/寄存器）与设备SN绑定，并配置解析规则，将原始数据解析为设备状态/属性。
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_device_collect_binding")
public class DeviceCollectBinding extends EraEntity {

    private String connectId;

    /** 设备 SN，对应 Equip.selfCode */
    private String equipSn;

    /** 数据源标识：MQTT 为 topic，OPC 为 nodeId，Modbus 可为 "slaveId:startAddr" */
    private String sourceKey;

    /** 解析规则 JSON：如 {"runState":"$.payload.run","onlineState":"$.payload.online","attrs.temp":"$.payload.temperature"}，支持 JSONPath */
    private String parseRuleJson;

    public static DeviceCollectBindingDto covert(DeviceCollectBinding source) {
        if (source == null) return null;
        DeviceCollectBindingDto dto = new DeviceCollectBindingDto();
        BeanUtils.copyProperties(source, dto);
        return dto;
    }

    public static List<DeviceCollectBindingDto> covert(List<DeviceCollectBinding> sources) {
        List<DeviceCollectBindingDto> list = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) sources.forEach(s -> list.add(covert(s)));
        return list;
    }

    public static DeviceCollectBinding wrap(DeviceCollectBindingDto source) {
        if (source == null) return null;
        DeviceCollectBinding e = new DeviceCollectBinding();
        BeanUtils.copyProperties(source, e);
        return e;
    }
}
