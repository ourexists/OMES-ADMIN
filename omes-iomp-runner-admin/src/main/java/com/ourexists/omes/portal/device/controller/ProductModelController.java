package com.ourexists.omes.portal.device.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.device.feign.ProductModelFeign;
import com.ourexists.omes.device.model.ProductModelDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "产品型号")
@RestController
@RequestMapping("/productModel")
public class ProductModelController {

    @Autowired
    private ProductModelFeign productModelFeign;

    @Operation(summary = "按产品编号列出型号")
    @GetMapping("listByProductCode")
    public JsonResponseEntity<List<ProductModelDto>> listByProductCode(@RequestParam String productCode) {
        return productModelFeign.listByProductCode(productCode);
    }

    @Operation(summary = "根据ID查询型号")
    @GetMapping("selectById")
    public JsonResponseEntity<ProductModelDto> selectById(@RequestParam String id) {
        return productModelFeign.selectById(id);
    }

    @Operation(summary = "新增或修改型号")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody ProductModelDto dto) {
        return productModelFeign.addOrUpdate(dto);
    }

    @Operation(summary = "删除型号")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return productModelFeign.delete(idsDto);
    }
}
