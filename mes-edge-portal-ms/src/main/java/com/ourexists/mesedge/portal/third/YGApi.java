/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.third;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.DateUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.portal.third.model.req.CompleteReq;
import com.ourexists.mesedge.portal.third.model.resp.Order;
import com.ourexists.mesedge.sync.feign.ConnectFeign;
import com.ourexists.mesedge.sync.model.ConnectDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    private ConnectFeign connectFeign;

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

    private String getUri() {
        ConnectDto connect;
        try {
            connect = RemoteHandleUtils.getDataFormResponse(connectFeign.selectConnectByName(SERVER_NAME));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        String uri = connect.getHost() + ":" + connect.getPort();
        if (StringUtils.isNotBlank(connect.getSuffix())) {
            uri += "/" + connect.getSuffix();
        }
        return uri;
    }
}
