/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.ucenter.systemconfig.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.omes.ucenter.systemconfig.SystemConfigDto;
import com.ourexists.omes.ucenter.systemconfig.SystemConfigValue;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "t_system_config", autoResultMap = true)
public class SystemConfig extends MainEntity {

    private String configKey;

    @TableField(value = "config", typeHandler = JacksonTypeHandler.class)
    private SystemConfigValue config;

    public static SystemConfigDto toDto(SystemConfig source) {
        if (source == null) {
            return null;
        }
        SystemConfigDto dto = new SystemConfigDto();
        BeanUtils.copyProperties(source, dto);
        if (dto.getConfig() == null) {
            dto.setConfig(new SystemConfigValue());
        }
        return dto;
    }

    public static SystemConfig fromDto(SystemConfigDto dto) {
        if (dto == null) {
            return null;
        }
        SystemConfig entity = new SystemConfig();
        BeanUtils.copyProperties(dto, entity);
        if (entity.getConfig() == null) {
            entity.setConfig(new SystemConfigValue());
        }
        return entity;
    }
}
