package com.ourexists.mesedge.portal.device;

import com.alibaba.fastjson2.JSONObject;
import com.ourexists.mesedge.device.model.WorkshopScadaConfigDetail;
import lombok.extern.slf4j.Slf4j;
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
    private RestTemplate restTemplate;

    public static final String SCADA_PATH = "https://cloud.haiwell.com/api/project/getProjectMachineVisitUrl";

    @Override
    public String remote(WorkshopScadaConfigDetail workshopScadaConfigDetail, Integer platform) {
        log.info("【HaiWell api调用器】[{}]开始调用", SCADA_PATH);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", workshopScadaConfigDetail.getPrivateKey());
        jsonObject.put("privateKey", workshopScadaConfigDetail.getPrivateSecret());
        jsonObject.put("machineCode", workshopScadaConfigDetail.getMapCode());
        jsonObject.put("platform", platform);
        // 构建请求实体
        HttpEntity<String> entity = new HttpEntity<>(jsonObject.toJSONString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(SCADA_PATH, HttpMethod.POST, entity, String.class);
        log.info("【HaiWell api调用器】[{}]调用selectOrder成功,响应[{}]", SCADA_PATH, response.getBody());
        JSONObject jo = JSONObject.parseObject(response.getBody());
        if (jo == null) {
            return null;
        }
        JSONObject result = jo.getJSONObject("result");
        if (result == null) {
            log.error("【HaiWell api调用器】[{}]调用异常", SCADA_PATH);
            return null;
        }
        return result.getString("url");
    }

    @Override
    public String serverName() {
        return "HaiWell";
    }
}
