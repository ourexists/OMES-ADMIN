package com.ourexists.omes.device.core.equip.protocol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProtocolExecutor {

    @Autowired
    private List<ProtocolManager> protocolManagers;

    public boolean start(ProtocolConnect connect) {
        ProtocolManager protocolManager = protocolManager(connect.getProtocol());
        if (protocolManager == null) {
            return true;
        }
        return protocolManager.start(connect);
    }

    public boolean stop(String protocol, String connectId) {
        ProtocolManager protocolManager = protocolManager(protocol);
        if (protocolManager == null) {
            return true;
        }
        return protocolManager.stop(connectId);
    }


    public ProtocolManager protocolManager(String name) {
        for (ProtocolManager protocolManager : protocolManagers) {
            if (protocolManager.protocol().equals(name)) {
                return protocolManager;
            }
        }
        return null;
    }
}
