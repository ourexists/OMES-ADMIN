package com.ourexists.mesedge.portal.device;


import com.ourexists.mesedge.device.model.WorkshopScadaConfigDetail;

public interface ScadaRemoteRequester {

    String remote(WorkshopScadaConfigDetail workshopScadaConfigDetail, Integer platform);

    String serverName();
}
