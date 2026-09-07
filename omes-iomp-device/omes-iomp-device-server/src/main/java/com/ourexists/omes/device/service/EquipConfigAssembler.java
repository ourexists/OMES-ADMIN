package com.ourexists.omes.device.service;

import com.ourexists.omes.device.model.EquipConfigDetail;
import com.ourexists.omes.device.model.GwBindingDto;
import com.ourexists.omes.device.model.ProductAttrMerge;
import com.ourexists.omes.device.pojo.GwBinding;
import com.ourexists.omes.device.pojo.Product;
import com.ourexists.omes.device.pojo.ProductModel;
import org.springframework.stereotype.Component;

/**
 * 设备采集配置：网关来自设备绑定，属性映射来自产品模板 + 型号。
 */
@Component
public class EquipConfigAssembler {

    public GwBindingDto assemble(String equipId, GwBinding binding, Product product, ProductModel model) {
        String gwId = binding == null ? null : binding.getGwId();
        EquipConfigDetail modelConfig = model == null ? null : model.getAttrConfig();
        EquipConfigDetail merged = ProductAttrMerge.merge(
                product == null ? null : product.getAttrConfig(),
                modelConfig);
        if (gwId != null && !gwId.isEmpty()) {
            merged.setGwId(gwId);
        }
        GwBindingDto dto = new GwBindingDto();
        dto.setEquipId(equipId);
        dto.setGwId(gwId);
        dto.setConfig(merged);
        return dto;
    }
}
