package com.ourexists.omes.portal.device.protocol;

import com.ourexists.omes.device.core.equip.protocol.ProtocolConnect;
import com.ourexists.omes.device.core.equip.protocol.ProtocolManager;
import org.springframework.stereotype.Component;

@Component
public class OPCUaPollingProtocolManager implements ProtocolManager {
    @Override
    public boolean start(ProtocolConnect connect) {
        return true;
    }

    @Override
    public boolean stop(String connectId) {
        return true;
    }

    @Override
    public void stopAll() {

    }

    @Override
    public String protocol() {
        return "OPC UA";
    }
}
