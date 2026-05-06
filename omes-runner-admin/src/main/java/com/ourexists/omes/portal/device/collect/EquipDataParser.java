package com.ourexists.omes.portal.device.collect;

import com.ourexists.omes.device.core.equip.cache.EquipRealtime;

import java.util.List;

public interface EquipDataParser {

    /**
     * 解析网关查询的数据
     * @param gwId          网关id
     * @param sourceData    数据
     * @return
     */
    List<EquipRealtime> parse(String gwId, String sourceData);
}
