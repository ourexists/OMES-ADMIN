/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task;

import com.alibaba.fastjson2.JSON;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.mesedge.portal.config.CacheUtils;
import com.ourexists.mesedge.portal.config.MqttSender;
import com.ourexists.mesedge.portal.sync.remote.WinccApi;
import com.ourexists.mesedge.portal.sync.remote.model.Datalist;
import com.ourexists.mesedge.report.feign.WinCCReportFeign;
import com.ourexists.mesedge.report.model.WinCCDatalistDto;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static com.ourexists.mesedge.portal.task.WinCCDevConstants.DATA_CACHE;

@Slf4j
@Component("CollectWinCCDatalist")
public class CollectWinCCDatalistTimerTask extends TimerTask {

    @Autowired
    private WinccApi winccApi;

    @Autowired
    private MqttSender mqttSender;

    @Autowired
    private CacheUtils cacheUtils;

    @Override
    public void doRun() {
        UserContext.defaultTenant();
        Datalist datalist = winccApi.pullTags("dataList", Datalist.class);
        if (datalist == null) {
            return;
        }
        datalist.setExecTime(new Date());
        WinCCDatalistDto r = Datalist.covert(datalist);
        cacheUtils.put(DATA_CACHE, "dataList", r);
        mqttSender.send("data/datalist", JSON.toJSONString(r));
    }
}
