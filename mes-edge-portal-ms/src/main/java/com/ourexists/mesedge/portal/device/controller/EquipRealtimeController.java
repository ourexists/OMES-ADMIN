package com.ourexists.mesedge.portal.device.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.mesedge.device.core.EquipRealtime;
import com.ourexists.mesedge.device.core.EquipRealtimeManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "实时设备")
@RestController
@Slf4j
@RequestMapping("/equip_realtime")
public class EquipRealtimeController {

    @Autowired
    private EquipRealtimeManager manager;

    @Operation(summary = "获取所有设备", description = "获取所有设备")
    @GetMapping("getAllRealtime")
    public JsonResponseEntity<List<EquipRealtime>> getAllRealtime() {
        Map<String, List<EquipRealtime>> m = manager.getAll(UserContext.getTenant().getTenantId());
        List<EquipRealtime> r = new ArrayList<>();
        for (List<EquipRealtime> value : m.values()) {
            r.addAll(value);
        }
        return JsonResponseEntity.success(r);
    }

    @Operation(summary = "获取场景所有设备", description = "获取场景所有设备")
    @GetMapping("getAllRealtimeInWorkshop")
    public JsonResponseEntity<List<EquipRealtime>> getAllRealtimeInWorkshop(@RequestParam String workshopCode) {
        List<EquipRealtime> m = manager.getAll(UserContext.getTenant().getTenantId(), workshopCode);
        return JsonResponseEntity.success(m);
    }

}
