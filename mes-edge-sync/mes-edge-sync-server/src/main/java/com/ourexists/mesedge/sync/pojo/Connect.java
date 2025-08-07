/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.sync.model.ConnectDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_connect")
public class Connect extends EraEntity {

    /**
     * 服务名
     */
    private String serverName;

    /**
     * ip
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    private String suffix;

    private String protocol;

    public static ConnectDto covert(Connect source) {
        if (source == null) {
            return null;
        }
        ConnectDto target = new ConnectDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<ConnectDto> covert(List<Connect> sources) {
        List<ConnectDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static Connect wrap(ConnectDto source) {
        Connect target = new Connect();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<Connect> wrap(List<ConnectDto> sources) {
        List<Connect> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
