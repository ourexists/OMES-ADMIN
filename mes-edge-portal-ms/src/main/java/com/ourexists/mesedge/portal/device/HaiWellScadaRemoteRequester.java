package com.ourexists.mesedge.portal.device;

import com.alibaba.fastjson2.JSONObject;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.device.model.WorkshopScadaConfigDetail;
import com.ourexists.mesedge.sync.feign.ConnectFeign;
import com.ourexists.mesedge.sync.model.ConnectDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class HaiWellScadaRemoteRequester implements ScadaRemoteRequester {

    @Autowired
    private ConnectFeign connectFeign;

    @Autowired
    private RestTemplate restTemplate;

    public static final String SCADA_PATH = "/api/project/getProjectMachineVisitUrl";

    @Override
    public String remote(WorkshopScadaConfigDetail workshopScadaConfigDetail) {
        String url = getAccessUri() + SCADA_PATH;
        log.info("【HaiWell api调用器】[{}]开始调用", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", workshopScadaConfigDetail.getPrivateKey());
        jsonObject.put("privateKey", workshopScadaConfigDetail.getPrivateSecret());
        jsonObject.put("machineCode", workshopScadaConfigDetail.getMapCode());
        jsonObject.put("platform", 1);
        // 构建请求实体
        HttpEntity<String> entity = new HttpEntity<>(jsonObject.toJSONString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        log.info("【HaiWell api调用器】[{}]调用selectOrder成功,响应[{}]", url, response.getBody());
        JSONObject jo = JSONObject.parseObject(response.getBody());
        if (jo == null) {
            return null;
        }
        JSONObject result = jo.getJSONObject("result");
        if (result == null) {
            log.error("【HaiWell api调用器】[{}]调用异常", url);
            return null;
        }
        return result.getString("url");
    }

    @Override
    public String serverName() {
        return "HaiWell";
    }

    private String getAccessUri() {
        ConnectDto connect;
        try {
            connect = RemoteHandleUtils.getDataFormResponse(connectFeign.selectConnectByName(serverName()));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        String uri = connect.getHost();
        if (connect.getPort() != null) {
            uri += ":" + connect.getPort();
        }
        if (StringUtils.isNotBlank(connect.getSuffix())) {
            uri += "/" + connect.getSuffix();
        }
        return uri;
    }
}
