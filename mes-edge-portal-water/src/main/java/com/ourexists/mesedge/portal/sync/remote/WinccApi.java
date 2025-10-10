/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.sync.remote;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Maps;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.DateUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.portal.sync.remote.model.Datalist;
import com.ourexists.mesedge.sync.enums.ConnectValidTypeEnum;
import com.ourexists.mesedge.sync.feign.ConnectFeign;
import com.ourexists.mesedge.sync.model.ConnectDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WinccApi {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ConnectFeign connectFeign;

    public static final String SERVER_NAME = "WINCC_API";

    public static final String ARCHIVE_PATH = "/TagLogging/Archive/datalist/values";

    public Datalist pullDatalist(Date begin, Date end) {
        ConnectDto connect = connect();
        String url = getUri(connect) + ARCHIVE_PATH;
        Map<String, String> uriVariables = Maps.newHashMap();
        uriVariables.put("begin", DateUtil.dateTimeFormat(begin));
        uriVariables.put("end", DateUtil.dateTimeFormat(end));

        List<Map<String, List<String>>> body = new ArrayList<>();
        Map<String, List<String>> params = Maps.newHashMap();
        List<String> variableNames = new ArrayList<>();
        for (Field field : Datalist.class.getFields()) {
            variableNames.add(field.getName());
        }
        params.put("variableNames", variableNames);
        body.add(params);
        log.info("【yg api调用器】[{}]开始调用", url);
        HttpEntity<String> httpEntity = new HttpEntity<>(JSON.toJSONString(body), httpHeaders(connect));
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class, uriVariables);
        if (resp.getStatusCode() == HttpStatus.OK) {
            String msg = resp.getBody();
            log.info("【yg api调用器】[{}]调用pullDatalist成功,响应[{}]", url, msg);
            JSONArray jsonArray = JSON.parseArray(msg);
            if (jsonArray == null || jsonArray.isEmpty()) {
                return null;
            }
            Datalist datalist = new Datalist();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String field = jsonObject.getString("variableName").replace("_", "");
                JSONArray values = jsonObject.getJSONArray("values");
                if (values == null || values.isEmpty()) {
                    continue;
                }
                Float value = values.getFloat(0);
                try {
                    Method method = Datalist.class.getMethod("set" + field, Float.class);
                    method.invoke(datalist, value);
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
                         InvocationTargetException ignored) {
                }
            }
            return datalist;
        } else {
            log.info("【yg api调用器】[{}]调用pullDatalist网络异常,异常码[{}]", url, resp.getStatusCode());
            return null;
        }
    }

    private HttpHeaders httpHeaders(ConnectDto connect) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json;charset=UTF-8");
        ConnectValidTypeEnum validTypeEnum = ConnectValidTypeEnum.valueOf(connect.getValidType());
        switch (validTypeEnum) {
            case BASIC:
                String auth = connect.getUsername() + ":" + connect.getPassword();
                byte[] originAuth = auth.getBytes(StandardCharsets.US_ASCII);
                byte[] encodedAuth = Base64.encodeBase64(originAuth);
                httpHeaders.add("Authorization", "Basic " + new String(encodedAuth));
                break;
            default:
                break;
        }
        return httpHeaders;
    }

    private String getUri(ConnectDto connect) {
        String uri = connect.getHost() + ":" + connect.getPort();
        if (StringUtils.isNotBlank(connect.getSuffix())) {
            uri += "/" + connect.getSuffix();
        }
        return uri;
    }

    private ConnectDto connect() {
        ConnectDto connect;
        try {
            return RemoteHandleUtils.getDataFormResponse(connectFeign.selectConnectByName(SERVER_NAME));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
    }
}
