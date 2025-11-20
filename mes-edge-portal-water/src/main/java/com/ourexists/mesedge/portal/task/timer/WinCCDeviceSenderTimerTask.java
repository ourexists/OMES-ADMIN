package com.ourexists.mesedge.portal.task.timer;

import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.ourexists.mesedge.portal.config.CacheUtils;
import com.ourexists.mesedge.portal.config.MqttSender;
import com.ourexists.mesedge.portal.task.WinccDeviceCount;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static com.ourexists.mesedge.portal.task.WinCCDevConstants.CACHE_LIST;

@Slf4j
@Component("WinCCDeviceSender")
public class WinCCDeviceSenderTimerTask extends TimerTask {

    @Autowired
    private MqttSender mqttSender;

    @Autowired
    private CacheUtils cacheUtils;

    @Override
    public void doRun() {
        Integer totalRun = 0;
        Integer totalStop = 0;
        Integer totalAlarm = 0;

        JSONObject jo = new JSONObject();
        Map<String, Integer> deviceMap = new HashMap<>();
        for (String cacheName : CACHE_LIST) {
            WinccDeviceCount count = count(cacheName);
            deviceMap.putAll(count.getDevice());

            //设备态推送
            JSONObject devJO = new JSONObject(deviceMap);
            mqttSender.send("data/dev/" + cacheName, devJO.toJSONString());

            //合计
            totalRun += count.getRun();
            totalStop += count.getStop();
            totalAlarm += count.getAlarm();

            //单设备态统计
            jo.put(cacheName + "_run", count.getRun());
            jo.put(cacheName + "_stop", count.getStop());
            jo.put(cacheName + "_alarm", count.getAlarm());
        }

        jo.put("totalRun", totalRun);
        jo.put("totalStop", totalStop);
        jo.put("totalAlarm", totalAlarm);
        mqttSender.send("data/count", jo.toString());
    }

    private WinccDeviceCount count(String cacheName) {
        Cache<Object, Object> runCache = cacheUtils.nativeCache(cacheName);
        ConcurrentMap<Object, Object> runCacheMap = runCache.asMap();
        WinccDeviceCount winccDeviceCount = new WinccDeviceCount();
        Map<String, Integer> device = new HashMap<>();
        int run = 0;
        int stop = 0;
        for (Map.Entry<Object, Object> entry : runCacheMap.entrySet()) {
            Integer val = (Integer) entry.getValue();
            device.put(entry.getKey().toString() + "_run", val);
            if (val == 0) {
                stop++;
            } else if (val == 1) {
                run++;
            }
        }

        Cache<Object, Object> alarmCache = cacheUtils.nativeCache(cacheName + "_alarm");
        ConcurrentMap<Object, Object> alarmCacheMap = alarmCache.asMap();
        int alarm = 0;
        for (Map.Entry<Object, Object> entry : alarmCacheMap.entrySet()) {
            Boolean val = (Boolean) entry.getValue();
            int r = 0;
            if (val) {
                alarm++;
                r = 1;
            }
            device.put(entry.getKey().toString() + "_alarm", r);
        }
        winccDeviceCount.setRun(run);
        winccDeviceCount.setStop(stop);
        winccDeviceCount.setAlarm(alarm);
        winccDeviceCount.setDevice(device);
        return winccDeviceCount;
    }
}
