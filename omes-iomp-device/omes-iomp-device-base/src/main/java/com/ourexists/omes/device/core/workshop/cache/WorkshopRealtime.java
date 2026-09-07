package com.ourexists.omes.device.core.workshop.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class WorkshopRealtime {

    private String id;

    private WorkshopRealtimeConfig config;

    private List<WorkshopRealtimeCollect> attrsRealtime;

    private Date time;

    /** 租户（门户缓存与流式出站 JSON 携带，供下游按租户入库）。 */
    private String tenantId;

    /**
     * 进入 Flink 作业时的单调序号（反序列化时赋值）；{@code time} 相同或为空时用于比较先后。
     */
    private Long streamIngressSeq;
}
