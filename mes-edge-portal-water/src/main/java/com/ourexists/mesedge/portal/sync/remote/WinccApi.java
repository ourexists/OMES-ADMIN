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
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.portal.config.CacheUtils;
import com.ourexists.mesedge.portal.sync.remote.constants.StructureTypeEnum;
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WinccApi {

    private RestTemplate restTemplate;

    @Autowired
    private ConnectFeign connectFeign;

    public static final String SERVER_NAME = "WINCC_API";

    public static final String ARCHIVE_PATH = "/TagLogging/Archive";

    public static final String TAG_PATH = "/tagManagement/Values";

    public static final String DEV_RUN = ".2RunState";

    public static final String DEV_ALARM = ".3Alarm";

    @Autowired
    private CacheUtils cacheUtils;

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

    public <T> T pullTags(Class<T> clazz) {
        return pullTags(clazz, StructureTypeEnum.def, null);
    }


    private <T> List<String> generateVariableNames(Class<T> clazz,
                                                   StructureTypeEnum structureType) {
        List<String> variableNames = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            WinCCVari ano = field.getAnnotation(WinCCVari.class);
            String fieldName;
            if (ano == null || StringUtils.isBlank(ano.value())) {
                fieldName = field.getName();
            } else {
                fieldName = ano.value();
            }
            if (StructureTypeEnum.dev.equals(structureType)) {
                variableNames.add(fieldName + DEV_ALARM);
                fieldName += DEV_RUN;
            }
            variableNames.add(fieldName);
        }
        return variableNames;
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

    public <T> T pullTags(Class<T> clazz,
                          StructureTypeEnum structureType,
                          String cacheName) {
        ConnectDto connect = connect();
        String url = getUri(connect) + TAG_PATH;
        Map<String, List<String>> params = Maps.newHashMap();
        params.put("variableNames", generateVariableNames(clazz, structureType));
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
            T r;
            try {
                r = clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                log.error("pullTags error", e);
                throw new RuntimeException(e);
            }
            int success = 0;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String error = jsonObject.getString("error");
                if (StringUtils.isNotEmpty(error)) {
                    continue;
                }
                String field = jsonObject.getString("variableName");
                //这里不处理报警相关内容
                if (field.endsWith(DEV_ALARM)) {
                    continue;
                }

                Integer dataType = jsonObject.getInteger("dataType");
                if (dataType == null) {
                    continue;
                }
                Object value = parseValue(jsonObject);
                if (value == null) {
                    continue;
                }
                //代表设备，进行本地实时缓存
                if (StructureTypeEnum.dev.equals(structureType)) {
                    field = field.replace(DEV_RUN, "");
                }
                try {
                    String fieldName = setValForAnno(r, field, value);
                    if (fieldName == null) {
                        continue;
                    }
                    if (StructureTypeEnum.dev.equals(structureType)) {
                        cacheUtils.put(cacheName, fieldName, value);
                    }
                    success++;
                } catch (IllegalAccessException e) {
                    log.error("", e);
                }
            }
            if (success == 0) {
                return null;
            }

            //处理报警交集
            if (StructureTypeEnum.dev.equals(structureType)) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String error = jsonObject.getString("error");
                    if (StringUtils.isNotEmpty(error)) {
                        continue;
                    }
                    String field = jsonObject.getString("variableName");
                    if (field.endsWith(DEV_ALARM)) {
                        boolean value = jsonObject.getBooleanValue("value");
                        //PAM搅拌机报警反的
                        if (field.contains("PAM_Blender")) {
                            value = !value;
                        }
                        field = field.replace(DEV_ALARM, "");
                        String fieldName = getFieldNameForAnno(r, field);
                        if (fieldName == null) {
                            continue;
                        }
                        //代表设备，进行本地报警缓存
                        cacheUtils.put(cacheName + "_alarm", fieldName, value);
                        if (!value) {
                            continue;
                        }
                        try {
                            Field f = r.getClass().getDeclaredField(fieldName);
                            f.setAccessible(true);
                            f.set(r, 3);
                            f.setAccessible(false);
                            setValForAnno(r, field, 3);
                        } catch (IllegalAccessException | NoSuchFieldException e) {
                            log.error("", e);
                        }
                    }
                }
            }
            return r;
        } else {
            log.info("【yg api调用器】[{}]调用WINCC_REST网络异常,异常码[{}]", url, resp.getStatusCode());
            return null;
        }
    }

    private static <T> String getFieldNameForAnno(T r, String annoVal) {
        String realFieldName = null;
        for (Field field : r.getClass().getDeclaredFields()) {
            WinCCVari ano = field.getAnnotation(WinCCVari.class);
            String fieldName;
            if (ano == null || StringUtils.isBlank(ano.value())) {
                fieldName = field.getName();
            } else {
                fieldName = ano.value();
            }
            if (fieldName.equals(annoVal)) {
                realFieldName = field.getName();
            }
        }
        return realFieldName;
    }

    /**
     * 返回设置的缓存名称
     * @param r
     * @param annoVal
     * @param value
     * @return
     * @param <T>
     * @throws IllegalAccessException
     */
    private static <T> String setValForAnno(T r, String annoVal, Object value) throws IllegalAccessException {
        String realFieldName = null;
        for (Field field : r.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            WinCCVari ano = field.getAnnotation(WinCCVari.class);
            String fieldName;
            if (ano == null || StringUtils.isBlank(ano.value())) {
                fieldName = field.getName();
            } else {
                fieldName = ano.value();
            }
            if (fieldName.equals(annoVal)) {
                realFieldName = field.getName();
                field.set(r, value);
                field.setAccessible(false);
            }
        }
        return realFieldName;
    }

    public <T> T pullArchive(String tagGroupName,
                             Class<T> clazz,
                             boolean isDevice,
                             String deviceCacheName) {
        ConnectDto connect = connect();
        String url = getUri(connect) + ARCHIVE_PATH + "/" + tagGroupName + "/values?maxValues=1";
        Map<String, List<String>> params = Maps.newHashMap();
        List<String> variableNames = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals("startTime") || field.getName().equals("endTime") || field.getName().equals("execTime")
                    || field.getName().equals("id")) {
                continue;
            }
            variableNames.add(field.getName());
            if (isDevice) {
                variableNames.add(field.getName() + "Alarm");
            }
        }
        params.put("variableNames", variableNames);
        HttpEntity<String> httpEntity = new HttpEntity<>(JSON.toJSONString(params), httpHeaders(connect));
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
        if (resp.getStatusCode() == HttpStatus.OK) {
            String msg = resp.getBody();
            JSONArray jsonArray = JSON.parseArray(msg);
            if (jsonArray == null || jsonArray.isEmpty()) {
                return null;
            }
            T r;
            try {
                r = clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                log.error("pullTags error", e);
                throw new RuntimeException(e);
            }
            int success = 0;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String error = jsonObject.getString("error");
                if (StringUtils.isNotEmpty(error)) {
                    continue;
                }
                String field = jsonObject.getString("variableName");
                JSONArray values = jsonObject.getJSONArray("values");
                if (values == null || values.isEmpty()) {
                    continue;
                }
                JSONObject valuer = values.getJSONObject(0);
                String vals = valuer.getString("value");
                Object value;
                try {
                    value = Float.parseFloat(vals);
                } catch (NumberFormatException e) {
                    boolean b = Boolean.parseBoolean(vals);
                    if (b) {
                        value = 1;
                    } else {
                        value = 0;
                    }
                }
                //代表设备，进行本地实时缓存
                if (field.contains("Alarm")) {
                    continue;
                }
                if (isDevice) {
                    cacheUtils.put(deviceCacheName, field, value);
                }
                try {
                    String setterName = "set" + getMethodName(field);
                    Method setter = Arrays.stream(clazz.getMethods())
                            .filter(m -> m.getName().equalsIgnoreCase(setterName))
                            .findFirst()
                            .orElse(null);
                    if (setter != null) {
                        setter.invoke(r, value);
                        success++;
                    }
                } catch (SecurityException | IllegalAccessException |
                         InvocationTargetException ignored) {
                }
            }
            if (success == 0) {
                return null;
            }

            //处理报警交集
            if (isDevice) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String error = jsonObject.getString("error");
                    if (StringUtils.isNotEmpty(error)) {
                        continue;
                    }
                    String field = jsonObject.getString("variableName");
                    if (field.endsWith("Alarm")) {
                        JSONArray values = jsonObject.getJSONArray("values");
                        if (values == null || values.isEmpty()) {
                            continue;
                        }
                        JSONObject valuer = values.getJSONObject(0);
                        boolean value = valuer.getBooleanValue("value");
                        if (!value) {
                            continue;
                        }
                        field = field.replace("Alarm", "");
                        //代表设备，进行本地报警缓存
                        cacheUtils.put(deviceCacheName + "_alarm", field, value);
                        try {
                            String setterName = "set" + getMethodName(field);
                            Method setter = Arrays.stream(clazz.getMethods())
                                    .filter(m -> m.getName().equalsIgnoreCase(setterName))
                                    .findFirst()
                                    .orElse(null);
                            if (setter != null) {
                                setter.invoke(r, 3);
                                success++;
                            }
                        } catch (SecurityException | IllegalAccessException |
                                 InvocationTargetException ignored) {
                        }
                    }
                }
            }
            return r;
        } else {
            log.info("【yg api调用器】[{}]调用WINCC_REST归档网络异常,异常码[{}]", url, resp.getStatusCode());
            return null;
        }
    }

    /**
     * 首字母大写(进行字母的ascii编码前移，效率是最高的)
     *
     * @param fieldName 需要转化的字符串
     */
    private static String getMethodName(String fieldName) {
        char[] chars = fieldName.toCharArray();
        chars[0] = toUpperCase(chars[0]);
        return String.valueOf(chars);
    }


    private static char toUpperCase(char c) {
        if (97 <= c && c <= 122) {
            c ^= 32;
        }
        return c;
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
        try {
            return RemoteHandleUtils.getDataFormResponse(connectFeign.selectConnectByName(SERVER_NAME));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
    }
}
