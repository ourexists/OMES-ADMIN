/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.omes.device.model.EquipHealthRuleTemplateConfig;
import com.ourexists.omes.device.model.EquipHealthRuleTemplateDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "t_equip_health_rule_template", autoResultMap = true)
public class EquipHealthRuleTemplate extends EraEntity {

    private String name;

    @TableField(value = "config", typeHandler = JacksonTypeHandler.class)
    private EquipHealthRuleTemplateConfig config;

    public static EquipHealthRuleTemplateDto toDto(EquipHealthRuleTemplate source) {
        if (source == null) return null;
        EquipHealthRuleTemplateDto dto = new EquipHealthRuleTemplateDto();
        dto.setId(source.getId());
        dto.setName(source.getName());
        dto.setConfig(source.getConfig() != null ? source.getConfig() : new EquipHealthRuleTemplateConfig());
        return dto;
    }

    public static EquipHealthRuleTemplate fromDto(EquipHealthRuleTemplateDto dto) {
        if (dto == null) return null;
        EquipHealthRuleTemplate entity = new EquipHealthRuleTemplate();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setConfig(dto.getConfig() != null ? dto.getConfig() : new EquipHealthRuleTemplateConfig());
        return entity;
    }
}
