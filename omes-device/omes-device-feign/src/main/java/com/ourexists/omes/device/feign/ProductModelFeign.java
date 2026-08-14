package com.ourexists.omes.device.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.device.model.ProductModelDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ProductModelFeign {

    @GetMapping("listByProductCode")
    JsonResponseEntity<List<ProductModelDto>> listByProductCode(@RequestParam String productCode);

    @GetMapping("selectById")
    JsonResponseEntity<ProductModelDto> selectById(@RequestParam String id);

    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody ProductModelDto dto);

    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);
}
