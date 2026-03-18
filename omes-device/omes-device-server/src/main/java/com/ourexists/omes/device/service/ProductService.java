/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.device.model.ProductPageQuery;
import com.ourexists.omes.device.pojo.Product;

import java.util.List;

public interface ProductService extends IMyBatisPlusService<Product> {

    Page<Product> selectByPage(ProductPageQuery query);

    List<Product> listAll();

    /** 按产品编号查询 */
    Product getByCode(String code);

    List<Product> getByCode(List<String> codes);
}
