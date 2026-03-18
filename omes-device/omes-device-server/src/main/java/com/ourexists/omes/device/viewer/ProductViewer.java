/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.omes.device.feign.ProductFeign;
import com.ourexists.omes.device.model.ProductDto;
import com.ourexists.omes.device.model.ProductPageQuery;
import com.ourexists.omes.device.pojo.Product;
import com.ourexists.omes.device.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
public class ProductViewer implements ProductFeign {

    @Autowired
    private ProductService productService;

    @Override
    @Operation(summary = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<ProductDto>> selectByPage(@RequestBody ProductPageQuery query) {
        Page<Product> page = productService.selectByPage(query);
        return JsonResponseEntity.success(Product.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    @Override
    @Operation(summary = "根据ID查询")
    @GetMapping("selectById")
    public JsonResponseEntity<ProductDto> selectById(@RequestParam String id) {
        return JsonResponseEntity.success(Product.covert(productService.getById(id)));
    }

    @Override
    @Operation(summary = "查询全部，用于下拉")
    @GetMapping("listAll")
    public JsonResponseEntity<List<ProductDto>> listAll() {
        return JsonResponseEntity.success(Product.covert(productService.listAll()));
    }

    @Override
    @Operation(summary = "新增或修改")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody ProductDto dto) {
        productService.saveOrUpdate(Product.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        productService.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }
}
