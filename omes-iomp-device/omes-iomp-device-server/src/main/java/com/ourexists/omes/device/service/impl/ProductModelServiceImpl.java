package com.ourexists.omes.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.device.mapper.ProductModelMapper;
import com.ourexists.omes.device.pojo.ProductModel;
import com.ourexists.omes.device.service.ProductModelService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class ProductModelServiceImpl extends AbstractMyBatisPlusService<ProductModelMapper, ProductModel>
        implements ProductModelService {

    @Override
    public List<ProductModel> listByProductCode(String productCode) {
        if (!StringUtils.hasText(productCode)) {
            return Collections.emptyList();
        }
        return this.list(new LambdaQueryWrapper<ProductModel>()
                .eq(ProductModel::getProductCode, productCode)
                .orderByAsc(ProductModel::getCode));
    }

    @Override
    public List<ProductModel> listByProductCodes(List<String> productCodes) {
        if (productCodes == null || productCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return this.list(new LambdaQueryWrapper<ProductModel>()
                .in(ProductModel::getProductCode, productCodes)
                .orderByAsc(ProductModel::getProductCode)
                .orderByAsc(ProductModel::getCode));
    }

    @Override
    public ProductModel getByProductAndCode(String productCode, String code) {
        if (!StringUtils.hasText(productCode) || !StringUtils.hasText(code)) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<ProductModel>()
                .eq(ProductModel::getProductCode, productCode)
                .eq(ProductModel::getCode, code)
                .last("limit 1"));
    }
}
