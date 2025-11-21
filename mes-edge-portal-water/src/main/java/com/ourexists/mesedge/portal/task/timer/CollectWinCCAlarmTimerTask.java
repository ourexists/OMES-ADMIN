package com.ourexists.mesedge.portal.task.timer;

import com.alibaba.fastjson2.JSON;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.mesedge.portal.config.MqttSender;
import com.ourexists.mesedge.portal.sync.remote.WinccApi;
import com.ourexists.mesedge.portal.sync.remote.model.AlarmVari;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("CollectWinCCAlarm")
public class CollectWinCCAlarmTimerTask  extends TimerTask {

    @Autowired
    private WinccApi winccApi;

    @Autowired
    private MqttSender mqttSender;


    @Override
    public void doRun() {
        UserContext.defaultTenant();
        AlarmVari r = winccApi.pullTags(AlarmVari.class);
        mqttSender.send("data/alarm", JSON.toJSONString(r));
    }
}
