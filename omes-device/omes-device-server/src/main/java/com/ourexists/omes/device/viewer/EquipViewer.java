/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.viewer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.omes.device.core.equip.cache.EquipAttrRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipControlRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipRealtimeManager;
import com.ourexists.omes.device.feign.EquipFeign;
import com.ourexists.omes.device.model.*;
import com.ourexists.omes.device.pojo.Equip;
import com.ourexists.omes.device.pojo.GwBinding;
import com.ourexists.omes.device.pojo.Product;
import com.ourexists.omes.device.pojo.Workshop;
import com.ourexists.omes.device.service.EquipService;
import com.ourexists.omes.device.service.GwBindingService;
import com.ourexists.omes.device.service.ProductService;
import com.ourexists.omes.device.service.WorkshopService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EquipViewer implements EquipFeign {

    @Autowired
    private EquipService service;

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Autowired
    private WorkshopService workshopService;

    @Autowired
    private GwBindingService gwBindingService;

    @Autowired
    private ProductService productService;

    @Override
    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EquipDto>> selectByPage(@RequestBody EquipPageQuery dto) {
        Page<Equip> page = service.selectByPage(dto);
        List<EquipDto> r = Equip.covert(page.getRecords());
        if (!CollectionUtils.isEmpty(r)) {
            List<String> productCodes = r.stream().map(EquipDto::getType).filter(code -> code != null && !code.isEmpty()).distinct().toList();
            Map<String, String> codeToName = new HashMap<>();
            Map<String, String> codeToImageUrl = new HashMap<>();
            List<Product> p = productService.getByCode(productCodes);
            if (!CollectionUtils.isEmpty(p)) {
                for (Product pp : p) {
                    codeToName.put(pp.getCode(), pp.getName());
                    if (pp.getImageUrl() != null && !pp.getImageUrl().isEmpty()) {
                        codeToImageUrl.put(pp.getCode(), pp.getImageUrl());
                    }
                }
            }
            for (EquipDto equipDto : r) {
                if (equipDto.getType() != null) {
                    equipDto.setTypeDesc(codeToName.get(equipDto.getType()));
                    equipDto.setProductImage(codeToImageUrl.get(equipDto.getType()));
                }
            }
            List<WorkshopTreeNode> workshopDtos = null;
            List<GwBindingDto> equipConfigs = null;
            if (dto.getQueryWorkshop()) {
                List<String> codes = r.stream().map(EquipDto::getWorkshopCode).toList();
                workshopDtos = Workshop.covert(workshopService.queryByCodes(codes));
            }
            if (dto.getQueryConfig()) {
                List<String> ids = r.stream().map(EquipDto::getId).toList();
                equipConfigs = GwBinding.covert(gwBindingService.queryByEquip(ids));
            }
            for (EquipDto equipDto : r) {
                if (!CollectionUtils.isEmpty(workshopDtos)) {
                    for (WorkshopTreeNode workshopDto : workshopDtos) {
                        if (equipDto.getWorkshopCode().equals(workshopDto.getSelfCode())) {
                            equipDto.setWorkshop(workshopDto);
                            break;
                        }
                    }
                }
                if (!CollectionUtils.isEmpty(equipConfigs)) {
                    for (GwBindingDto gwBindingDto : equipConfigs) {
                        if (gwBindingDto.getEquipId().equals(equipDto.getId())) {
                            equipDto.setConfig(gwBindingDto);
                            break;
                        }
                    }
                }
                if (dto.getNeedRealtime()) {
                    EquipRealtime equipRealtime = equipRealtimeManager.get(equipDto.getSelfCode());
                    if (equipRealtime != null) {
                        BeanUtils.copyProperties(equipRealtime, equipDto, "name", "selfCode");
                        List<EquipAttr> attrs = new ArrayList<>();
                        if (!CollectionUtils.isEmpty(equipRealtime.getEquipAttrRealtimes())) {
                            for (EquipAttrRealtime equipAttrRealtime : equipRealtime.getEquipAttrRealtimes()) {
                                EquipAttr attrDto = new EquipAttr();
                                BeanUtils.copyProperties(equipAttrRealtime, attrDto);
                                attrs.add(attrDto);
                            }
                        }
                        equipDto.setAttrs(attrs);
                        equipDto.setAlarmTexts(equipRealtime.getAlarmTexts());
                    }
                }
            }
        }
        return JsonResponseEntity.success(r, OrmUtils.extraPagination(page));
    }

    @Override
    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody EquipDto dto) {
        Equip e = Equip.wrap(dto);
        service.saveOrUpdate(e);
        EquipRealtime equipRealtime = equipRealtimeManager.get(dto.getSelfCode());
        if (equipRealtime == null) {
            equipRealtime = new EquipRealtime();
        }
        BeanUtils.copyProperties(e, equipRealtime);
        equipRealtimeManager.addOrUpdate(equipRealtime);
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.removeByIds(idsDto.getIds());
        equipRealtimeManager.reload();
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "通过id查询", description = "通过id查询")
    @GetMapping("selectById")
    public JsonResponseEntity<EquipDto> selectById(@RequestParam String id, @RequestParam Boolean needRealtime) {
        EquipDto equipDto = Equip.covert(service.getById(id));
        if (equipDto == null) {
            return null;
        }
        if (equipDto.getType() != null && !equipDto.getType().isEmpty()) {
            Product p = productService.getByCode(equipDto.getType());
            if (p != null) {
                equipDto.setTypeDesc(p.getName());
                if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
                    equipDto.setProductImage(p.getImageUrl());
                }
            }
        }
        equipDto.setWorkshop(Workshop.covert(workshopService.queryByCode(equipDto.getWorkshopCode())));
        if (needRealtime == null || !needRealtime) {
            return JsonResponseEntity.success(equipDto);
        }
        EquipRealtime equipRealtime = equipRealtimeManager.get(equipDto.getSelfCode());
        if (equipRealtime != null) {
            BeanUtils.copyProperties(equipRealtime, equipDto, "name", "selfCode");
            List<EquipAttr> attrs = new ArrayList<>();
            if (!CollectionUtils.isEmpty(equipRealtime.getEquipAttrRealtimes())) {
                for (EquipAttrRealtime equipAttrRealtime : equipRealtime.getEquipAttrRealtimes()) {
                    EquipAttr attrDto = new EquipAttr();
                    BeanUtils.copyProperties(equipAttrRealtime, attrDto);
                    attrs.add(attrDto);
                }
            }
            equipDto.setAttrs(attrs);
            List<EquipControl> controls = new ArrayList<>();
            if (!CollectionUtils.isEmpty(equipRealtime.getEquipControlRealtimes())) {
                for (EquipControlRealtime controlRealtime : equipRealtime.getEquipControlRealtimes()) {
                    EquipControl attrDto = new EquipControl();
                    BeanUtils.copyProperties(controlRealtime, attrDto);
                    controls.add(attrDto);
                }
            }
            equipDto.setControls(controls);
        }
        return JsonResponseEntity.success(equipDto);
    }

    @Override
    @Operation(summary = "查询设备配置", description = "查询设备配置")
    @GetMapping("queryEquipConfig")
    public JsonResponseEntity<GwBindingDto> queryEquipConfig(@RequestParam String equipId) {
        return JsonResponseEntity.success(GwBinding.covert(gwBindingService.queryByEquip(equipId)));
    }

    @Override
    @Operation(summary = "设置设备配置", description = "设置设备配置")
    @GetMapping("setEquipConfig")
    public JsonResponseEntity<Boolean> setEquipConfig(@Validated @RequestBody GwBindingDto gwBindingDto) {
        gwBindingService.addOrUpdate(gwBindingDto);
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<GwBindingDto> queryEquipConfigBySn(String equipSn) {
        Equip equip = service.getOne(new LambdaQueryWrapper<Equip>().eq(Equip::getSelfCode, equipSn));
        if (equip == null) {
            return JsonResponseEntity.success(null);
        }
        return JsonResponseEntity.success(GwBinding.covert(gwBindingService.queryByEquip(equip.getId())));
    }

    @Override
    @Operation(summary = "根据ID批量查询设备", description = "用于关联展示设备名称等，仅返回基础字段")
    @PostMapping("listByIds")
    public JsonResponseEntity<List<EquipDto>> listByIds(@Validated @RequestBody List<String> ids) {
        List<Equip> list = service.listByIds(ids);
        return JsonResponseEntity.success(Equip.covert(list));
    }
}
