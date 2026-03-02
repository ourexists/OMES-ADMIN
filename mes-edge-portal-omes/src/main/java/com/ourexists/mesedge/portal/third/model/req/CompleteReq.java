/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.third.model.req;

import com.ourexists.mesedge.mps.model.MPSTFVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Setter
@Getter
@Accessors(chain = true)
public class CompleteReq {

    /**
     * 料框编码
     */
    private String frameNumber;

    /**
     * 设备编码
     */
    private String deviceNumber = "AFPE_1";

    /**
     * 设备加工记录
     */
    private List<MPSTFVo> processingRecords;
}
