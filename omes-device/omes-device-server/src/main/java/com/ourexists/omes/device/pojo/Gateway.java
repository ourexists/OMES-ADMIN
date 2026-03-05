/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.omes.device.model.GatewayDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_gateway")
public class Gateway extends EraEntity {

    /**
     * 服务名
     */
    private String serverName;

    /**
     * ip
     */
    private String uri;

    private String topic;

    private String params;

    private String protocol;

    /** 采集 cron 表达式，与 collectIntervalSec 二选一 */
    private String collectCron;

    private Integer validType;

    private Boolean enabled;

    private String username;

    private String password;

    public static GatewayDto covert(Gateway source) {
        if (source == null) {
            return null;
        }
        GatewayDto target = new GatewayDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<GatewayDto> covert(List<Gateway> sources) {
        List<GatewayDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static Gateway wrap(GatewayDto source) {
        Gateway target = new Gateway();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<Gateway> wrap(List<GatewayDto> sources) {
        List<Gateway> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
