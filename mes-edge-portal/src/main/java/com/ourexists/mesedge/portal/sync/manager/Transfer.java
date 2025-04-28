/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.sync.manager;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class Transfer {

    /**
     * 同步id主体
     */
    private String syncId;

    /**
     * 分片开始时间戳
     */
    private Date startTime;

    /**
     * 分片结束时间戳
     */
    private Date endTime;

    /**
     * 区间主键最小值
     */
    private String lastPartMin;

    /**
     * 区间主键最大值
     */
    private String lastPartMax;

    /**
     * 当前同步的流程名
     */
    private String flowName;

    /**
     * 当前同步流转的数据
     */
    private String jsonData;
}
