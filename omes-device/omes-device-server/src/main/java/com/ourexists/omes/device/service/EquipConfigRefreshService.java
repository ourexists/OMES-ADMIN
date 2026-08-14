package com.ourexists.omes.device.service;

import com.ourexists.omes.device.model.GwBindingDto;
import com.ourexists.omes.device.pojo.Equip;
import com.ourexists.omes.device.pojo.GwBinding;
import com.ourexists.omes.device.pojo.Product;
import com.ourexists.omes.device.pojo.ProductModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 按产品模板 + 型号映射 + 设备网关组装采集配置，并刷新实时缓存。
 */
@Service
public class EquipConfigRefreshService {

    @Autowired
    private EquipService equipService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductModelService productModelService;

    @Autowired
    private GwBindingService gwBindingService;

    @Autowired
    private EquipConfigAssembler equipConfigAssembler;

    public GwBindingDto assemble(Equip equip) {
        if (equip == null) {
            return null;
        }
        return assemble(equip, gwBindingService.queryByEquip(equip.getId()));
    }

    public GwBindingDto assemble(Equip equip, GwBinding binding) {
        if (equip == null) {
            return null;
        }
        return equipConfigAssembler.assemble(
                equip.getId(),
                binding,
                loadProduct(equip.getType()),
                loadModel(equip.getModelId()));
    }

    public Map<String, GwBindingDto> assembleAll(List<Equip> equips) {
        Map<String, GwBindingDto> result = new HashMap<>();
        if (CollectionUtils.isEmpty(equips)) {
            return result;
        }
        List<String> equipIds = equips.stream().map(Equip::getId).filter(StringUtils::hasText).toList();
        Map<String, GwBinding> bindingByEquip = indexBindings(gwBindingService.queryByEquip(equipIds));
        Map<String, Product> productByCode = indexProducts(equips);
        Map<String, ProductModel> modelById = indexModels(equips);
        for (Equip equip : equips) {
            if (equip == null || !StringUtils.hasText(equip.getId())) {
                continue;
            }
            result.put(equip.getId(), equipConfigAssembler.assemble(
                    equip.getId(),
                    bindingByEquip.get(equip.getId()),
                    productByCode.get(equip.getType()),
                    modelById.get(equip.getModelId())));
        }
        return result;
    }

    public void refresh(Equip equip) {
        GwBindingDto assembled = assemble(equip);
        if (assembled != null) {
            gwBindingService.applyAssembledConfig(assembled);
        }
    }

    public void refreshByModelId(String modelId) {
        if (!StringUtils.hasText(modelId)) {
            return;
        }
        List<Equip> equips = equipService.listByModelIds(List.of(modelId));
        refreshAll(equips);
    }

    public void refreshByProductCode(String productCode) {
        if (!StringUtils.hasText(productCode)) {
            return;
        }
        refreshAll(equipService.listByProductCodes(List.of(productCode)));
    }

    private void refreshAll(List<Equip> equips) {
        if (CollectionUtils.isEmpty(equips)) {
            return;
        }
        Map<String, GwBindingDto> assembled = assembleAll(equips);
        for (GwBindingDto dto : assembled.values()) {
            gwBindingService.applyAssembledConfig(dto);
        }
    }

    private Product loadProduct(String productCode) {
        return StringUtils.hasText(productCode) ? productService.getByCode(productCode) : null;
    }

    private ProductModel loadModel(String modelId) {
        return StringUtils.hasText(modelId) ? productModelService.getById(modelId) : null;
    }

    private Map<String, GwBinding> indexBindings(List<GwBinding> bindings) {
        Map<String, GwBinding> map = new HashMap<>();
        if (CollectionUtils.isEmpty(bindings)) {
            return map;
        }
        for (GwBinding binding : bindings) {
            if (binding != null && StringUtils.hasText(binding.getEquipId())) {
                map.put(binding.getEquipId(), binding);
            }
        }
        return map;
    }

    private Map<String, Product> indexProducts(List<Equip> equips) {
        List<String> codes = equips.stream()
                .map(Equip::getType)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        Map<String, Product> map = new HashMap<>();
        if (codes.isEmpty()) {
            return map;
        }
        List<Product> products = productService.getByCode(codes);
        if (CollectionUtils.isEmpty(products)) {
            return map;
        }
        for (Product product : products) {
            if (product != null && StringUtils.hasText(product.getCode())) {
                map.put(product.getCode(), product);
            }
        }
        return map;
    }

    private Map<String, ProductModel> indexModels(List<Equip> equips) {
        List<String> ids = equips.stream()
                .map(Equip::getModelId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        Map<String, ProductModel> map = new HashMap<>();
        if (ids.isEmpty()) {
            return map;
        }
        List<ProductModel> models = productModelService.listByIds(ids);
        if (CollectionUtils.isEmpty(models)) {
            return map;
        }
        for (ProductModel model : models) {
            if (model != null && StringUtils.hasText(model.getId())) {
                map.put(model.getId(), model);
            }
        }
        return map;
    }

    public Map<String, ProductModel> mapByIds(List<String> modelIds) {
        Map<String, ProductModel> map = new HashMap<>();
        List<String> ids = modelIds == null ? List.of() : modelIds.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return map;
        }
        List<ProductModel> models = productModelService.listByIds(ids);
        if (CollectionUtils.isEmpty(models)) {
            return map;
        }
        for (ProductModel model : models) {
            if (model != null && StringUtils.hasText(model.getId())) {
                map.put(model.getId(), model);
            }
        }
        return map;
    }
}
