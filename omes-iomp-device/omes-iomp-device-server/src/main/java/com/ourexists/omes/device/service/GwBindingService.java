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

    /** 仅持久化设备与网关绑定，属性映射以型号为准 */
    void saveGatewayBinding(String equipId, String gwId);

    /** 将已组装的产品模板+型号映射写入实时缓存 */
    void applyAssembledConfig(GwBindingDto dto);

    void delete(List<String> ids);

    List<GwBinding> queryByEquip(List<String> equipIds);

    GwBinding queryByEquip(String equipId);
}
