/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.device.model.ProductDto;
import com.ourexists.omes.device.model.ProductPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ProductFeign {

    @PostMapping("selectByPage")
    JsonResponseEntity<List<ProductDto>> selectByPage(@RequestBody ProductPageQuery query);

    @GetMapping("selectById")
    JsonResponseEntity<ProductDto> selectById(@RequestParam String id);

    @GetMapping("listAll")
    JsonResponseEntity<List<ProductDto>> listAll();

    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody ProductDto dto);

    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);
}
