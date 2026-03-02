/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.third.wincc;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Maps;
import com.ourexists.mesedge.sync.enums.ConnectValidTypeEnum;
import com.ourexists.mesedge.sync.feign.ConnectFeign;
import com.ourexists.mesedge.sync.model.ConnectDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WinccApi {

    private RestTemplate restTemplate;



    public static final String TAG_PATH = "/tagManagement/Values";


    public WinccApi() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        // 1️⃣ 构建信任所有证书的 SSLContext
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, (chain, authType) -> true) // 信任所有
                .build();

        // 2️⃣ 构建 SSL 连接工厂
        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();

        // 3️⃣ 构建 HttpClient
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(
                        PoolingHttpClientConnectionManagerBuilder.create()
                                .setSSLSocketFactory(sslSocketFactory)
                                .build()
                )
                .build();

        // 4️⃣ 构建 RequestFactory
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(30));

        // 5️⃣ 构建 RestTemplate
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().stream()
                .filter(c -> c instanceof StringHttpMessageConverter)
                .forEach(c -> ((StringHttpMessageConverter) c).setDefaultCharset(StandardCharsets.UTF_8));
        this.restTemplate = restTemplate;
    }

    private Object parseValue(JSONObject jsonObject) {
        Integer dataType = jsonObject.getInteger("dataType");
        if (dataType == null) {
            return null;
        }
        Object value = null;
        if (dataType == 1) {
            boolean b = jsonObject.getBooleanValue("value");
            if (b) {
                value = 1;
            } else {
                value = 0;
            }
        } else if (dataType == 4) {
            value = jsonObject.getFloatValue("value");
        }
        return value;
    }

    public Map<String, Object> pullTags(ConnectDto connect, List<String> variableNames) {
        String url = getUri(connect) + TAG_PATH;
        Map<String, List<String>> params = Maps.newHashMap();
        params.put("variableNames", variableNames);
        log.info("【yg api调用器】[{}]开始调用[{}]", url, JSON.toJSONString(params));
        HttpEntity<String> httpEntity = new HttpEntity<>(JSON.toJSONString(params), httpHeaders(connect));
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
        if (resp.getStatusCode() == HttpStatus.OK) {
            String msg = resp.getBody();
            log.info("【yg api调用器】[{}]调用pullTags成功", url);
            JSONArray jsonArray = JSON.parseArray(msg);
            if (jsonArray == null || jsonArray.isEmpty()) {
                return null;
            }
            Map<String, Object> result = new HashMap<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String error = jsonObject.getString("error");
                if (StringUtils.isNotEmpty(error)) {
                    continue;
                }
                String field = jsonObject.getString("variableName");

                Integer dataType = jsonObject.getInteger("dataType");
                if (dataType == null) {
                    continue;
                }
                Object value = parseValue(jsonObject);
                if (value == null) {
                    continue;
                }
                result.put(field, value);
            }
            return result;
        } else {
            log.info("【yg api调用器】[{}]调用WINCC_REST网络异常,异常码[{}]", url, resp.getStatusCode());
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


}
