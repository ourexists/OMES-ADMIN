package com.ourexists.omes.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.device.model.DeviceTreeNode;
import com.ourexists.omes.device.model.DgEquipProcessDto;
import com.ourexists.omes.device.model.EquipDto;
import com.ourexists.omes.device.model.EquipMatDto;
import com.ourexists.omes.device.pojo.DgEquip;
import com.ourexists.omes.device.pojo.Equip;
import com.ourexists.omes.device.pojo.EquipMat;
import com.ourexists.omes.device.pojo.Product;
import com.ourexists.omes.device.pojo.Workshop;
import com.ourexists.omes.device.service.DgEquipService;
import com.ourexists.omes.device.service.EquipMatService;
import com.ourexists.omes.device.service.EquipProcessService;
import com.ourexists.omes.device.service.EquipService;
import com.ourexists.omes.device.service.ProductService;
import com.ourexists.omes.device.service.WorkshopService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EquipProcessServiceImpl implements EquipProcessService {

    @Autowired
    private EquipMatService equipMatService;
    @Autowired
    private DgEquipService dgEquipService;
    @Autowired
    private EquipService equipService;
    @Autowired
    private ProductService productService;
    @Autowired
    private WorkshopService workshopService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindEquips(String dgId, List<String> equipIds) {
        if (StringUtils.isBlank(dgId)) {
            throw new BusinessException("设备能力不能为空");
        }
        if (CollectionUtils.isEmpty(equipIds)) {
            return;
        }
        List<String> ids = equipIds.stream().filter(StringUtils::isNotBlank).distinct().toList();
        if (ids.isEmpty()) {
            return;
        }
        Set<String> exists = dgEquipService.list(new LambdaQueryWrapper<DgEquip>()
                        .eq(DgEquip::getDgId, dgId)
                        .in(DgEquip::getEquipId, ids))
                .stream()
                .map(DgEquip::getEquipId)
                .collect(Collectors.toSet());
        List<DgEquip> rows = new ArrayList<>();
        for (String equipId : ids) {
            if (exists.contains(equipId)) {
                continue;
            }
            rows.add(new DgEquip().setDgId(dgId).setEquipId(equipId));
        }
        if (!rows.isEmpty()) {
            dgEquipService.saveBatch(rows);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindEquips(String dgId, List<String> equipIds) {
        if (StringUtils.isBlank(dgId) || CollectionUtils.isEmpty(equipIds)) {
            return;
        }
        dgEquipService.remove(new LambdaQueryWrapper<DgEquip>()
                .eq(DgEquip::getDgId, dgId)
                .in(DgEquip::getEquipId, equipIds));
        equipMatService.remove(new LambdaQueryWrapper<EquipMat>()
                .eq(EquipMat::getDgId, dgId)
                .in(EquipMat::getEquipId, equipIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindByDgIds(List<String> dgIds) {
        if (CollectionUtils.isEmpty(dgIds)) {
            return;
        }
        dgEquipService.remove(new LambdaQueryWrapper<DgEquip>().in(DgEquip::getDgId, dgIds));
        equipMatService.remove(new LambdaQueryWrapper<EquipMat>().in(EquipMat::getDgId, dgIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByEquipIds(List<String> equipIds) {
        if (CollectionUtils.isEmpty(equipIds)) {
            return;
        }
        equipMatService.remove(new LambdaQueryWrapper<EquipMat>().in(EquipMat::getEquipId, equipIds));
        dgEquipService.remove(new LambdaQueryWrapper<DgEquip>().in(DgEquip::getEquipId, equipIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProcess(DgEquipProcessDto dto) {
        if (dto == null || StringUtils.isBlank(dto.getDgId()) || StringUtils.isBlank(dto.getEquipId())) {
            throw new BusinessException("设备能力或设备不能为空");
        }
        DgEquip binding = dgEquipService.getOne(new LambdaQueryWrapper<DgEquip>()
                .eq(DgEquip::getDgId, dto.getDgId())
                .eq(DgEquip::getEquipId, dto.getEquipId())
                .last("limit 1"));
        if (binding == null) {
            throw new BusinessException("设备未绑定到该能力方案");
        }
        replaceMaterials(dto.getDgId(), dto.getEquipId(), dto.getProcessMaterials());
    }

    @Override
    public List<EquipDto> listBoundEquips(String dgId) {
        if (StringUtils.isBlank(dgId)) {
            return new ArrayList<>();
        }
        List<DgEquip> bindings = dgEquipService.list(new LambdaQueryWrapper<DgEquip>()
                .eq(DgEquip::getDgId, dgId)
                .orderByAsc(DgEquip::getId));
        if (bindings.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> equipIds = bindings.stream()
                .map(DgEquip::getEquipId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        Map<String, Equip> byId = equipService.listByIds(equipIds).stream()
                .collect(Collectors.toMap(Equip::getId, item -> item, (a, b) -> a, LinkedHashMap::new));
        Map<String, List<EquipMatDto>> materials = loadMaterials(dgId, equipIds);
        List<EquipDto> result = new ArrayList<>();
        for (DgEquip binding : bindings) {
            Equip equip = byId.get(binding.getEquipId());
            if (equip == null) {
                continue;
            }
            EquipDto dto = Equip.covert(equip);
            dto.setProcessMaterials(materials.getOrDefault(binding.getEquipId(), new ArrayList<>()));
            result.add(dto);
        }
        fillProductAndWorkshop(result);
        return result;
    }

    @Override
    public List<DeviceTreeNode> listBoundAsDeviceNodes(String dgId) {
        List<EquipDto> equips = listBoundEquips(dgId);
        List<DeviceTreeNode> nodes = new ArrayList<>();
        for (EquipDto equip : equips) {
            List<EquipMatDto> materials = CollectionUtils.isEmpty(equip.getProcessMaterials())
                    ? new ArrayList<>()
                    : equip.getProcessMaterials();
            if (materials.isEmpty()) {
                nodes.add(toDeviceNode(dgId, equip, null));
                continue;
            }
            for (EquipMatDto mat : materials) {
                nodes.add(toDeviceNode(dgId, equip, mat));
            }
        }
        return nodes;
    }

    private DeviceTreeNode toDeviceNode(String dgId, EquipDto equip, EquipMatDto mat) {
        DeviceTreeNode node = new DeviceTreeNode();
        node.setId(equip.getId());
        node.setName(equip.getName());
        node.setSelfCode(equip.getSelfCode());
        node.setDgId(dgId);
        if (mat != null && StringUtils.isNotBlank(mat.getMatCode())) {
            node.setMatCode(mat.getMatCode());
            node.setMaxCapacity(mat.getMaxCapacity());
        }
        return node;
    }

    @Override
    public boolean isUseMat(List<String> matCodes) {
        if (CollectionUtils.isEmpty(matCodes)) {
            return false;
        }
        List<String> codes = matCodes.stream().filter(StringUtils::isNotBlank).distinct().toList();
        if (codes.isEmpty()) {
            return false;
        }
        return equipMatService.count(new LambdaQueryWrapper<EquipMat>().in(EquipMat::getMatCode, codes)) > 0;
    }

    private void replaceMaterials(String dgId, String equipId, List<EquipMatDto> processMaterials) {
        equipMatService.remove(new LambdaQueryWrapper<EquipMat>()
                .eq(EquipMat::getDgId, dgId)
                .eq(EquipMat::getEquipId, equipId));
        if (CollectionUtils.isEmpty(processMaterials)) {
            return;
        }
        LinkedHashMap<String, EquipMatDto> unique = new LinkedHashMap<>();
        for (EquipMatDto item : processMaterials) {
            if (item == null || StringUtils.isBlank(item.getMatCode())) {
                continue;
            }
            String code = item.getMatCode().trim();
            unique.put(code, item);
        }
        if (unique.isEmpty()) {
            return;
        }
        List<EquipMat> rows = new ArrayList<>();
        for (EquipMatDto item : unique.values()) {
            rows.add(new EquipMat()
                    .setDgId(dgId)
                    .setEquipId(equipId)
                    .setMatCode(item.getMatCode().trim())
                    .setMaxCapacity(item.getMaxCapacity()));
        }
        equipMatService.saveBatch(rows);
    }

    private Map<String, List<EquipMatDto>> loadMaterials(String dgId, List<String> equipIds) {
        List<EquipMat> rows = equipMatService.list(new LambdaQueryWrapper<EquipMat>()
                .eq(EquipMat::getDgId, dgId)
                .in(EquipMat::getEquipId, equipIds)
                .orderByAsc(EquipMat::getId));
        Map<String, List<EquipMatDto>> map = new LinkedHashMap<>();
        for (EquipMat row : rows) {
            EquipMatDto dto = new EquipMatDto().setMatCode(row.getMatCode()).setMaxCapacity(row.getMaxCapacity());
            map.computeIfAbsent(row.getEquipId(), key -> new ArrayList<>()).add(dto);
        }
        return map;
    }

    private void fillProductAndWorkshop(List<EquipDto> equips) {
        if (CollectionUtils.isEmpty(equips)) {
            return;
        }
        List<String> productCodes = equips.stream()
                .map(EquipDto::getType)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        Map<String, String> typeName = new LinkedHashMap<>();
        if (!productCodes.isEmpty()) {
            List<Product> products = productService.getByCode(productCodes);
            if (!CollectionUtils.isEmpty(products)) {
                for (Product product : products) {
                    typeName.put(product.getCode(), product.getName());
                }
            }
        }
        List<String> workshopCodes = equips.stream()
                .map(EquipDto::getWorkshopCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        Map<String, Workshop> workshops = new LinkedHashMap<>();
        if (!workshopCodes.isEmpty()) {
            List<Workshop> list = workshopService.queryByCodes(workshopCodes);
            if (!CollectionUtils.isEmpty(list)) {
                for (Workshop workshop : list) {
                    workshops.put(workshop.getSelfCode(), workshop);
                }
            }
        }
        for (EquipDto equip : equips) {
            if (StringUtils.isNotBlank(equip.getType())) {
                equip.setTypeDesc(typeName.get(equip.getType()));
            }
            if (StringUtils.isNotBlank(equip.getWorkshopCode())) {
                Workshop workshop = workshops.get(equip.getWorkshopCode());
                if (workshop != null) {
                    equip.setWorkshop(Workshop.covert(workshop));
                }
            }
        }
    }
}
