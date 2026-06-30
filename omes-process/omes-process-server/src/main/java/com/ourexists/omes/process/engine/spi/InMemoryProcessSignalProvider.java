package com.ourexists.omes.process.engine.spi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存信号源，每个仿真会话独立实例；生产环境可实现 {@link ProcessSignalProvider} 对接设备。
 */
public class InMemoryProcessSignalProvider implements ProcessSignalProvider {

    private volatile double temperature;
    private volatile boolean completeConfirmed;
    private final Map<String, Boolean> states = new ConcurrentHashMap<>();

    @Override
    public double readTemperature() {
        return temperature;
    }

    @Override
    public boolean isStateActive(String stateToken) {
        return Boolean.TRUE.equals(states.get(normalize(stateToken)));
    }

    @Override
    public boolean isCompleteConfirmed(String confirmKey) {
        return completeConfirmed;
    }

    @Override
    public void resetCompleteConfirm(String confirmKey) {
        this.completeConfirmed = false;
    }

    public void setCompleteConfirmed(boolean confirmed) {
        this.completeConfirmed = confirmed;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setState(String stateToken, boolean active) {
        states.put(normalize(stateToken), active);
    }

    public void clearStates() {
        states.clear();
    }

    private static String normalize(String stateToken) {
        return stateToken == null ? "" : stateToken.trim().toUpperCase();
    }
}
