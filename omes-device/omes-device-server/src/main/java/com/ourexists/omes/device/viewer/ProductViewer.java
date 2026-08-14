/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.omes.device.feign.ProductFeign;
import com.ourexists.omes.device.model.ProductDto;
import com.ourexists.omes.device.model.ProductPageQuery;
import com.ourexists.omes.device.pojo.Product;
import com.ourexists.omes.device.pojo.ProductModel;
import com.ourexists.omes.device.service.EquipConfigRefreshService;
import com.ourexists.omes.device.service.EquipService;
import com.ourexists.omes.device.service.ProductModelService;
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

    @Autowired
    private ProductModelService productModelService;

    @Autowired
    private EquipService equipService;

    @Autowired
    private EquipConfigRefreshService equipConfigRefreshService;

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
    @Operation(summary = "根据产品编号查询")
    @GetMapping("selectByCode")
    public JsonResponseEntity<ProductDto> selectByCode(@RequestParam String code) {
        if (code == null || code.isBlank()) {
            return JsonResponseEntity.success(null);
        }
        return JsonResponseEntity.success(Product.covert(productService.getByCode(code)));
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
        Product entity = Product.wrap(dto);
        if (dto.getId() != null && !dto.getId().isBlank() && dto.getAttrConfig() == null) {
            Product existing = productService.getById(dto.getId());
            if (existing != null) {
                entity.setAttrConfig(existing.getAttrConfig());
            }
        }
        productService.saveOrUpdate(entity);
        if (dto.getAttrConfig() != null && entity.getCode() != null) {
            equipConfigRefreshService.refreshByProductCode(entity.getCode());
        }
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        List<Product> products = productService.listByIds(idsDto.getIds());
        List<String> codes = products == null ? List.of() : products.stream()
                .map(Product::getCode)
                .filter(code -> code != null && !code.isBlank())
                .toList();
        if (!codes.isEmpty()) {
            List<ProductModel> models = productModelService.listByProductCodes(codes);
            if (models != null && !models.isEmpty()) {
                throw new BusinessException("产品下存在型号，无法删除");
            }
            if (!equipService.listByProductCodes(codes).isEmpty()) {
                throw new BusinessException("产品已被设备引用，无法删除");
            }
        }
        productService.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }
}
