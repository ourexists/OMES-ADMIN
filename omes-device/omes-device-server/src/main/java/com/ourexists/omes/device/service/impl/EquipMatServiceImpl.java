package com.ourexists.omes.device.service.impl;

import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.device.mapper.EquipMatMapper;
import com.ourexists.omes.device.pojo.EquipMat;
import com.ourexists.omes.device.service.EquipMatService;
import org.springframework.stereotype.Service;

@Service
public class EquipMatServiceImpl extends AbstractMyBatisPlusService<EquipMatMapper, EquipMat>
        implements EquipMatService {
}
