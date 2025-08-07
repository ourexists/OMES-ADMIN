/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.sync.model.SyncResourceDto;
import com.ourexists.mesedge.sync.model.SyncResourceVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("r_sync_resource")
public class SyncResource extends MainEntity {

    /**
     * 同步事件id
     */
    private String syncId;

    /**
     * 同步点位
     */
    private String point;

    /**
     * 点位状态
     */
    private String status;

    /**
     * 当前入参数据
     */
    private String reqData;

    /**
     * 当前出参数据
     */
    private String respData;

    /**
     * 异常信息
     */
    private String excep;


    public static SyncResourceVo covert(SyncResource source) {
        if (source == null) {
            return null;
        }
        SyncResourceVo target = new SyncResourceVo();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<SyncResourceVo> covert(List<SyncResource> sources) {
        List<SyncResourceVo> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static <T extends SyncResourceDto> SyncResource wrap(T source) {
        SyncResource target = new SyncResource();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends SyncResourceDto> List<SyncResource> wrap(List<T> sources) {
        List<SyncResource> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
