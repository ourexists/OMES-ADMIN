/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.device.core.EquipRealtime;
import com.ourexists.mesedge.device.core.EquipRealtimeManager;
import com.ourexists.mesedge.device.feign.EquipFeign;
import com.ourexists.mesedge.device.model.EquipDto;
import com.ourexists.mesedge.device.model.EquipPageQuery;
import com.ourexists.mesedge.device.model.WorkshopTreeNode;
import com.ourexists.mesedge.device.pojo.Equip;
import com.ourexists.mesedge.device.pojo.Workshop;
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

import java.util.List;

@Component
public class EquipViewer implements EquipFeign {

    @Autowired
    private EquipService service;

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Autowired
    private WorkshopService workshopService;

    @Override
    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EquipDto>> selectByPage(@RequestBody EquipPageQuery dto) {
        Page<Equip> page = service.selectByPage(dto);
        List<EquipDto> r = Equip.covert(page.getRecords());
        if (!CollectionUtils.isEmpty(r) && dto.getNeedRealtime()) {
            List<WorkshopTreeNode> workshopDtos = null;
            if (dto.getQueryWorkshop()) {
                List<String> codes = r.stream().map(EquipDto::getWorkshopCode).toList();
                workshopDtos = Workshop.covert(workshopService.queryByCodes(codes));
            }

            for (EquipDto equipDto : r) {
                EquipRealtime equipRealtime = equipRealtimeManager.get(equipDto.getTenantId(), equipDto.getSelfCode());
                if (equipRealtime != null) {
                    equipDto.setRunState(equipRealtime.getRunState());
                    equipDto.setAlarmState(equipRealtime.getAlarmState());
                    equipDto.setOnlineState(equipRealtime.getOnlineState());
                }
                if (!CollectionUtils.isEmpty(workshopDtos)) {
                    for (WorkshopTreeNode workshopDto : workshopDtos) {
                        if (equipDto.getWorkshopCode().equals(workshopDto.getSelfCode())) {
                            equipDto.setWorkshop(workshopDto);
                            break;
                        }
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
        service.saveOrUpdate(Equip.wrap(dto));
        EquipRealtime equipRealtime = new EquipRealtime();
        BeanUtils.copyProperties(dto, equipRealtime);
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
    public JsonResponseEntity<EquipDto> selectById(@RequestParam String id) {
        return JsonResponseEntity.success(Equip.covert(service.getById(id)));
    }
}
