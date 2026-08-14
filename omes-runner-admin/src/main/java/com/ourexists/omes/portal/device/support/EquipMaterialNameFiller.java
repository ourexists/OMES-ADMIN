package com.ourexists.omes.portal.device.support;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.model.EquipDto;
import com.ourexists.omes.device.model.EquipMatDto;
import com.ourexists.omes.mat.feign.MATFeign;
import com.ourexists.omes.mat.model.MaterialDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class EquipMaterialNameFiller {

    @Autowired
    private MATFeign matFeign;

    public void fill(EquipDto equip) {
        if (equip == null) {
            return;
        }
        List<EquipDto> list = new ArrayList<>();
        list.add(equip);
        fill(list);
    }

    public void fill(List<EquipDto> equips) {
        if (CollectionUtil.isBlank(equips)) {
            return;
        }
        List<String> codes = new ArrayList<>();
        for (EquipDto equip : equips) {
            if (equip == null || CollectionUtil.isBlank(equip.getProcessMaterials())) {
                continue;
            }
            for (EquipMatDto mat : equip.getProcessMaterials()) {
                if (mat != null && StringUtils.isNotBlank(mat.getMatCode())) {
                    codes.add(mat.getMatCode());
                }
            }
        }
        if (codes.isEmpty()) {
            return;
        }
        Map<String, String> nameByCode = loadNames(codes);
        if (nameByCode.isEmpty()) {
            return;
        }
        for (EquipDto equip : equips) {
            if (equip == null || CollectionUtil.isBlank(equip.getProcessMaterials())) {
                continue;
            }
            for (EquipMatDto mat : equip.getProcessMaterials()) {
                if (mat != null && StringUtils.isNotBlank(mat.getMatCode())) {
                    String name = nameByCode.get(mat.getMatCode());
                    if (name != null) {
                        mat.setMatName(name);
                    }
                }
            }
        }
    }

    private Map<String, String> loadNames(List<String> codes) {
        Map<String, String> map = new LinkedHashMap<>();
        try {
            IdsDto idsDto = new IdsDto();
            idsDto.setIds(codes.stream().distinct().toList());
            List<MaterialDto> materials = RemoteHandleUtils.getDataFormResponse(matFeign.selectByCodes(idsDto));
            if (CollectionUtil.isBlank(materials)) {
                return map;
            }
            for (MaterialDto material : materials) {
                if (material != null && StringUtils.isNotBlank(material.getSelfCode())) {
                    map.put(material.getSelfCode(), material.getName());
                }
            }
        } catch (EraCommonException ignored) {
            return map;
        }
        return map;
    }
}
