/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.device.model.EquipPageQuery;
import com.ourexists.omes.device.pojo.Equip;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface EquipService extends IMyBatisPlusService<Equip> {

    Page<Equip> selectByPage(EquipPageQuery dto);
}
