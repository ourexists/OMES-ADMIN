package com.ourexists.omes.portal.device;


import com.ourexists.omes.device.model.WorkshopScadaConfigDetail;

public interface ScadaRemoteRequester {

    String remote(WorkshopScadaConfigDetail workshopScadaConfigDetail, Integer platform);

    String serverName();
}
