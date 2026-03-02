/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.device.model.EquipAttrPageQuery;
import com.ourexists.omes.device.model.EquipCollectDto;
import com.ourexists.omes.device.model.EquipCollectPageQuery;
import com.ourexists.omes.device.pojo.EquipCollect;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface EquipCollectService extends IMyBatisPlusService<EquipCollect> {

    Page<EquipCollect> selectByPage(EquipCollectPageQuery dto);

    void addOrUpdate(EquipCollectDto dto);

    void delete(List<String> ids);

    List<EquipCollect> queryByEquip(List<String> sns);

    EquipCollect queryByEquip(String sn);
}
