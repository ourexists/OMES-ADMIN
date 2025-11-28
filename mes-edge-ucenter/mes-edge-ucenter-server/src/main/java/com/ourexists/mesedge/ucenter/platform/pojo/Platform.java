/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.platform.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.ucenter.platform.PlatformDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:13
 * @since 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("p_ucenter_platform")
public class Platform extends MainEntity {

    private String name;

    private String code;


    public static Platform warp(PlatformDto source) {
        Platform target = new Platform();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static PlatformDto covert(Platform source) {
        PlatformDto target = new PlatformDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<PlatformDto> covert(List<Platform> sources) {
        List<PlatformDto> targets = new ArrayList<>();
        if (!CollectionUtils.isEmpty(sources)) {
            for (Platform source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }
}
