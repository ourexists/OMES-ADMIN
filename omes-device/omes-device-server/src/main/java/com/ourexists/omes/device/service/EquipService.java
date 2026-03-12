/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.device.model.EquipPageQuery;
import com.ourexists.omes.device.pojo.Equip;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface EquipService extends IMyBatisPlusService<Equip> {

    Page<Equip> selectByPage(EquipPageQuery dto);

    /** 按自编码(Sn)查询设备，用于健康计算取创建时间等 */
    Equip getBySelfCode(String selfCode);

    /** 查询关联了指定健康模板的所有设备 */
    List<Equip> listByHealthTemplateId(String healthTemplateId);
}
