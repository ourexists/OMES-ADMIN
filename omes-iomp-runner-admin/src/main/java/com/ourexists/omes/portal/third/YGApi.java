/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.third;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.DateUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.feign.GatewayFeign;
import com.ourexists.omes.device.model.GatewayDto;
import com.ourexists.omes.portal.third.model.req.CompleteReq;
import com.ourexists.omes.portal.third.model.resp.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class YGApi {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GatewayFeign gatewayFeign;

    public static final String SERVER_NAME = "YG_API";

    public static final String ORDER_PATH = "/admin-api/mcs/rest/WorkOrder/findDefectsByTime";

    public static final String COMPLETE_PATH = "/admin-api/mcs/rest/WorkOrder/defectsDetectionPlanEnd";

    public static final String START_PATH = "/admin-api/mcs/rest/WorkOrder/defectsDetectionPlanStart";

    public List<Order> selectOrder(Date begin, Date end) {
        String url = getUri() + ORDER_PATH + "?begin=" + DateUtil.dateTimeFormat(begin) + "&end=" + DateUtil.dateTimeFormat(end);
        log.info("【yg api调用器】[{}]开始调用", url);
        String msg = restTemplate.getForObject(url, String.class);
        log.info("【yg api调用器】[{}]调用selectOrder成功,响应[{}]", url, msg);
        JSONObject jsonObject = JSONObject.parseObject(msg);
        if (jsonObject.getInteger("code") != 0) {
            log.error("【yg api调用器】[{}]调用异常[{}]", url, jsonObject.getString("msg"));
            return null;
        }
        return JSONArray.parseArray(jsonObject.getString("data"), Order.class);
    }

    public void startPlan(String frameNumber) {
        JSONObject req = new JSONObject();
        req.put("frameNumber", frameNumber);
        req.put("deviceNumber", "AFPE_1");
        String url = getUri() + START_PATH;
        log.info("【yg api调用器】[{}]开始调用[{}]", url, req.toJSONString());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json;charset=UTF-8");
        HttpEntity<String> httpEntity = new HttpEntity<>(req.toJSONString(), httpHeaders);
        String msg = restTemplate.postForObject(url, httpEntity, String.class);
        log.info("【yg api调用器】[{}]调用pushRecord成功,响应[{}]", url, msg);
        JSONObject jsonObject = JSONObject.parseObject(msg);
        if (jsonObject.getInteger("code") != 0) {
            log.error("【yg api调用器】[{}]调用异常[{}]", url, jsonObject.getString("msg"));
            throw new BusinessException("yg api调用异常");
        }
    }

    public void pushRecord(CompleteReq req) {
        String url = getUri() + COMPLETE_PATH;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json;charset=UTF-8");
        log.info("【yg api调用器】[{}]开始调用[{}]", url, JSON.toJSONString(req));
        HttpEntity<String> httpEntity = new HttpEntity<>(JSON.toJSONString(req), httpHeaders);
        String msg = restTemplate.postForObject(url, httpEntity, String.class);
        log.info("【yg api调用器】[{}]调用pushRecord成功,响应[{}]", url, msg);
        JSONObject jsonObject = JSONObject.parseObject(msg);
        if (jsonObject.getInteger("code") != 0) {
            log.error("【yg api调用器】[{}]调用异常[{}]", url, jsonObject.getString("msg"));
            throw new BusinessException("yg api调用异常");
        }
    }

    /**
     * 计划异常中止。YG 当前无 abort 接口：仅告警日志，不抛错以免阻断本地 CANCEL。
     * TODO: 契约到位后实现真实 HTTP 调用。
     */
    public void abortPlan(String frameNumber) {
        log.warn("【yg api调用器】abortPlan stub — no YG abort API yet, frameNumber={}", frameNumber);
    }

    /**
     * MES 订单变更查询。无真实 API：默认返回空，避免误自动取消生产。
     * 可通过系统属性 omes.yg.order-change.mock=true 注入空安全 mock 说明日志。
     * TODO: 对接 YG 变更查询接口。
     */
    public List<com.ourexists.omes.portal.third.model.resp.OrderChange> selectOrderChanges(Date begin, Date end) {
        log.info("【yg api调用器】selectOrderChanges stub begin={} end={} — returns empty (safe default)", begin, end);
        return java.util.Collections.emptyList();
    }

    private String getUri() {
        GatewayDto connect;
        try {
            connect = RemoteHandleUtils.getDataFormResponse(gatewayFeign.selectConnectByName(SERVER_NAME));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        return connect.getUri();
    }
}
