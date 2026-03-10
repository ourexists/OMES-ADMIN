package com.ourexists.omes.device.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;


@Getter
@Setter
@Accessors(chain = true)
public class WorkshopConfigCollectDetail {

    /**
     * 场景属性配置，每个 attr 通过 gwId 关联网关
     * 结构：name/map/gwId/value/unit/needCollect
     * 由设备监控对象 needCollect 派生
     */
    private List<WorkshopConfigCollectAttr> attrs;
}
