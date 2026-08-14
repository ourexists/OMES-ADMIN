package com.ourexists.omes.device.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.device.pojo.ProductModel;

import java.util.List;

public interface ProductModelService extends IMyBatisPlusService<ProductModel> {

    List<ProductModel> listByProductCode(String productCode);

    List<ProductModel> listByProductCodes(List<String> productCodes);

    ProductModel getByProductAndCode(String productCode, String code);
}
