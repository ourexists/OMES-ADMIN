package com.ourexists.mesedge.portal.device;

import com.ourexists.mesedge.device.model.WorkshopScadaConfigDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ScadaPathManager {

    @Autowired
    private List<ScadaRemoteRequester> remoteRequesters;

    public String getScadaPath(WorkshopScadaConfigDetail workshopScadaConfigDetail) {
        ScadaRemoteRequester remoteRequester = null;
        for (ScadaRemoteRequester r : remoteRequesters) {
            if (r.serverName().equals(workshopScadaConfigDetail.getServer())) {
                remoteRequester = r;
            }
        }
        if (remoteRequester == null) {
            return null;
        }
        return remoteRequester.remote(workshopScadaConfigDetail);
    }

    public List<String> getAllRequesters() {
        List<String> requesters = new ArrayList<>();
        for (ScadaRemoteRequester r : remoteRequesters) {
            requesters.add(r.serverName());
        }
        return requesters;
    }
}
