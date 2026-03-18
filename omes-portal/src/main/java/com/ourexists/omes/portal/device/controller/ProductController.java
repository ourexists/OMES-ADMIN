/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.device.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.device.feign.ProductFeign;
import com.ourexists.omes.device.model.ProductDto;
import com.ourexists.omes.device.model.ProductPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "产品管理")
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductFeign productFeign;

    @Operation(summary = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<ProductDto>> selectByPage(@RequestBody ProductPageQuery query) {
        return productFeign.selectByPage(query);
    }

    @Operation(summary = "根据ID查询")
    @GetMapping("selectById")
    public JsonResponseEntity<ProductDto> selectById(@RequestParam String id) {
        return productFeign.selectById(id);
    }

    @Operation(summary = "查询全部（用于下拉）")
    @GetMapping("listAll")
    public JsonResponseEntity<List<ProductDto>> listAll() {
        return productFeign.listAll();
    }

    @Operation(summary = "新增或修改")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody ProductDto dto) {
        return productFeign.addOrUpdate(dto);
    }

    @Operation(summary = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return productFeign.delete(idsDto);
    }
}
