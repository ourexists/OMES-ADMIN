/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.pojo;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.sync.model.SyncDto;
import com.ourexists.mesedge.sync.model.SyncVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_sync")
public class Sync extends EraEntity {

    /**
     * 同步分类
     */
    private String syncTx;

    /**
     * 分片开始时间戳
     */
    private Date partStartTimestamp;

    /**
     * 分片结束时间戳
     */
    private Date partEndTimestamp;

    /**
     * 同步状态
     */
    private String status;

    /**
     * 上一分片的最小值
     */
    private String preMin;

    /**
     * 上一分片的最大值
     */
    private String preMax;

    /**
     * 分片最小值
     */
    private String partMin;

    /**
     * 分片最大值
     */
    private String partMax;

    public static SyncVo covert(Sync source) {
        if (source == null) {
            return null;
        }
        SyncVo target = new SyncVo();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<SyncVo> covert(List<Sync> sources) {
        List<SyncVo> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(covert(source)));
        }
        return targets;
    }


    public static Sync wrap(SyncDto source) {
        Sync target = new Sync();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static List<Sync> wrap(List<SyncDto> sources) {
        List<Sync> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            sources.forEach(source -> targets.add(wrap(source)));
        }
        return targets;
    }
}
