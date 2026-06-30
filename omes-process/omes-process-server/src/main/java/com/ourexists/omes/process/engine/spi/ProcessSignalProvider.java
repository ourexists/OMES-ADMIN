package com.ourexists.omes.process.engine.spi;

/**
 * 工艺执行期过程量采集（温度、压力、离散状态等），由设备层或仿真层实现。
 */
public interface ProcessSignalProvider {

    double readTemperature();

    default double readTemperature(String equipmentCode) {
        return readTemperature();
    }

    boolean isStateActive(String stateToken);

    default double readPressure() {
        return 0D;
    }

    default double readPressure(String equipmentCode) {
        return readPressure();
    }

    /**
     * 完成动作为「等待人工确认」时，是否已收到确认信号。
     */
    default boolean isCompleteConfirmed(String confirmKey) {
        return false;
    }

    /**
     * 进入等待人工确认阶段时重置确认状态。
     */
    default void resetCompleteConfirm(String confirmKey) {
    }
}
