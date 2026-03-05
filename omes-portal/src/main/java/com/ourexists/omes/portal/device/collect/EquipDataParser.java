package com.ourexists.omes.portal.device.collect;

import com.ourexists.omes.device.core.equip.cache.EquipRealtime;

public interface EquipDataParser {

    EquipRealtime doParse(EquipRealtime equipRealtime, Object realtimeData);

    String name();
}
