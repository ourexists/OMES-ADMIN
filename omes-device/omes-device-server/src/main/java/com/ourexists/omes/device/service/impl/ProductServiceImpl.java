/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.device.mapper.ProductMapper;
import com.ourexists.omes.device.model.ProductPageQuery;
import com.ourexists.omes.device.pojo.Product;
import com.ourexists.omes.device.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ProductServiceImpl extends AbstractMyBatisPlusService<ProductMapper, Product> implements ProductService {

    @Override
    public Page<Product> selectByPage(ProductPageQuery query) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .like(StringUtils.hasText(query.getName()), Product::getName, query.getName())
                .like(StringUtils.hasText(query.getCode()), Product::getCode, query.getCode())
                .orderByAsc(Product::getCode);
        return this.page(new Page<>(query.getPage(), query.getPageSize()), wrapper);
    }

    @Override
    public List<Product> listAll() {
        return this.list(new LambdaQueryWrapper<Product>()
                .orderByAsc(Product::getCode));
    }

    @Override
    public Product getByCode(String code) {
        return this.getOne(new LambdaQueryWrapper<Product>().eq(Product::getCode, code));
    }

    @Override
    public List<Product> getByCode(List<String> codes) {
        return this.list(new LambdaQueryWrapper<Product>()
                .in(Product::getCode, codes)
                .orderByAsc(Product::getId));
    }
}
