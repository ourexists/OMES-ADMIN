package com.ourexists.omes.device.service.impl;

import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.device.mapper.DgEquipMapper;
import com.ourexists.omes.device.pojo.DgEquip;
import com.ourexists.omes.device.service.DgEquipService;
import org.springframework.stereotype.Service;

@Service
public class DgEquipServiceImpl extends AbstractMyBatisPlusService<DgEquipMapper, DgEquip>
        implements DgEquipService {
}
