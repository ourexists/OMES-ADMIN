package com.ourexists.omes.device.viewer;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.device.feign.ProductModelFeign;
import com.ourexists.omes.device.model.ProductModelDto;
import com.ourexists.omes.device.pojo.Equip;
import com.ourexists.omes.device.pojo.ProductModel;
import com.ourexists.omes.device.service.EquipConfigRefreshService;
import com.ourexists.omes.device.service.EquipService;
import com.ourexists.omes.device.service.ProductModelService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
public class ProductModelViewer implements ProductModelFeign {

    @Autowired
    private ProductModelService productModelService;

    @Autowired
    private EquipService equipService;

    @Autowired
    private EquipConfigRefreshService equipConfigRefreshService;

    @Override
    @Operation(summary = "按产品编号列出型号")
    @GetMapping("listByProductCode")
    public JsonResponseEntity<List<ProductModelDto>> listByProductCode(@RequestParam String productCode) {
        return JsonResponseEntity.success(ProductModel.covert(productModelService.listByProductCode(productCode)));
    }

    @Override
    @Operation(summary = "根据ID查询型号")
    @GetMapping("selectById")
    public JsonResponseEntity<ProductModelDto> selectById(@RequestParam String id) {
        return JsonResponseEntity.success(ProductModel.covert(productModelService.getById(id)));
    }

    @Override
    @Operation(summary = "新增或修改型号")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody ProductModelDto dto) {
        if (!StringUtils.hasText(dto.getProductCode()) || !StringUtils.hasText(dto.getCode())
                || !StringUtils.hasText(dto.getName())) {
            throw new BusinessException("产品编号、型号名称和型号编号不能为空");
        }
        ProductModel existing = productModelService.getByProductAndCode(dto.getProductCode(), dto.getCode().trim());
        if (existing != null && (dto.getId() == null || !dto.getId().equals(existing.getId()))) {
            throw new BusinessException("同一产品下型号编号已存在");
        }
        ProductModel entity = ProductModel.wrap(dto);
        if (StringUtils.hasText(dto.getId()) && dto.getAttrConfig() == null) {
            ProductModel current = productModelService.getById(dto.getId());
            if (current != null) {
                entity.setAttrConfig(current.getAttrConfig());
            }
        }
        productModelService.saveOrUpdate(entity);
        if (StringUtils.hasText(entity.getId())) {
            equipConfigRefreshService.refreshByModelId(entity.getId());
        }
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "删除型号")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        List<Equip> used = equipService.listByModelIds(idsDto.getIds());
        if (used != null && !used.isEmpty()) {
            throw new BusinessException("型号已被设备引用，无法删除");
        }
        productModelService.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }
}
