/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.device.model.EquipAttrPageQuery;
import com.ourexists.mesedge.device.model.EquipConfigDto;
import com.ourexists.mesedge.device.pojo.EquipConfig;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface EquipConfigService extends IMyBatisPlusService<EquipConfig> {

    Page<EquipConfig> selectByPage(EquipAttrPageQuery dto);

    void addOrUpdate(EquipConfigDto dto);

    void delete(List<String> ids);

    List<EquipConfig> queryByEquip(List<String> equipIds);

    EquipConfig queryByEquip(String equipId);
}
