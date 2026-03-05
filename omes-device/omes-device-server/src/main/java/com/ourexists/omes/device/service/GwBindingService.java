/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.device.model.EquipAttrPageQuery;
import com.ourexists.omes.device.model.GwBindingDto;
import com.ourexists.omes.device.pojo.GwBinding;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface GwBindingService extends IMyBatisPlusService<GwBinding> {

    Page<GwBinding> selectByPage(EquipAttrPageQuery dto);

    void addOrUpdate(GwBindingDto dto);

    void delete(List<String> ids);

    List<GwBinding> queryByEquip(List<String> equipIds);

    GwBinding queryByEquip(String equipId);

//    List<EquipConfig> queryEquipConfigBySn(String equipSn);
}
