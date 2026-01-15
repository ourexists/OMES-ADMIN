/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.viewer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.device.core.EquipAttrRealtime;
import com.ourexists.mesedge.device.core.EquipRealtime;
import com.ourexists.mesedge.device.core.EquipRealtimeManager;
import com.ourexists.mesedge.device.feign.EquipFeign;
import com.ourexists.mesedge.device.model.*;
import com.ourexists.mesedge.device.pojo.Equip;
import com.ourexists.mesedge.device.pojo.EquipConfig;
import com.ourexists.mesedge.device.pojo.Workshop;
import com.ourexists.mesedge.device.service.EquipConfigService;
import com.ourexists.mesedge.device.service.EquipService;
import com.ourexists.mesedge.device.service.WorkshopService;
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
import java.util.List;

@Component
public class EquipViewer implements EquipFeign {

    @Autowired
    private EquipService service;

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Autowired
    private WorkshopService workshopService;

    @Autowired
    private EquipConfigService equipConfigService;

    @Override
    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EquipDto>> selectByPage(@RequestBody EquipPageQuery dto) {
        Page<Equip> page = service.selectByPage(dto);
        List<EquipDto> r = Equip.covert(page.getRecords());
        if (!CollectionUtils.isEmpty(r)) {
            List<WorkshopTreeNode> workshopDtos = null;
            List<EquipConfigDto> equipConfigs = null;
            if (dto.getQueryWorkshop()) {
                List<String> codes = r.stream().map(EquipDto::getWorkshopCode).toList();
                workshopDtos = Workshop.covert(workshopService.queryByCodes(codes));
            }
            if (dto.getQueryConfig()) {
                List<String> ids = r.stream().map(EquipDto::getId).toList();
                equipConfigs = EquipConfig.covert(equipConfigService.queryByEquip(ids));
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
                    for (EquipConfigDto equipConfigDto : equipConfigs) {
                        if (equipConfigDto.getEquipId().equals(equipDto.getId())) {
                            equipDto.setConfig(equipConfigDto);
                            break;
                        }
                    }
                }
                if (dto.getNeedRealtime()) {
                    EquipRealtime equipRealtime = equipRealtimeManager.get(equipDto.getTenantId(), equipDto.getSelfCode());
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
        EquipRealtime equipRealtime = equipRealtimeManager.get(UserContext.getTenant().getTenantId(), dto.getSelfCode());
        if (equipRealtime == null) {
            equipRealtime = new EquipRealtime();
        }
        BeanUtils.copyProperties(e, equipRealtime);
        equipRealtimeManager.addOrUpdate(UserContext.getTenant().getTenantId(), equipRealtime);
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
        equipDto.setWorkshop(Workshop.covert(workshopService.queryByCode(equipDto.getWorkshopCode())));
        if (needRealtime == null || !needRealtime) {
            return JsonResponseEntity.success(equipDto);
        }
        EquipRealtime equipRealtime = equipRealtimeManager.get(equipDto.getTenantId(), equipDto.getSelfCode());
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
        }
        return JsonResponseEntity.success(equipDto);
    }

    @Override
    @Operation(summary = "查询设备配置", description = "查询设备配置")
    @GetMapping("queryEquipConfig")
    public JsonResponseEntity<EquipConfigDto> queryEquipConfig(@RequestParam String equipId) {
        return JsonResponseEntity.success(EquipConfig.covert(equipConfigService.queryByEquip(equipId)));
    }

    @Override
    @Operation(summary = "设置设备配置", description = "设置设备配置")
    @GetMapping("setEquipConfig")
    public JsonResponseEntity<Boolean> setEquipConfig(@Validated @RequestBody EquipConfigDto equipConfigDto) {
        equipConfigService.addOrUpdate(equipConfigDto);
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<EquipConfigDto> queryEquipConfigBySn(String equipSn) {
        Equip equip = service.getOne(new LambdaQueryWrapper<Equip>().eq(Equip::getSelfCode, equipSn));
        if (equip == null) {
            return JsonResponseEntity.success(null);
        }
        return JsonResponseEntity.success(EquipConfig.covert(equipConfigService.queryByEquip(equip.getId())));
    }
}
