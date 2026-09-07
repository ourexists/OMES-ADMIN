package com.ourexists.omes.process.engine.support;

/**
 * 简易 PID 控制器，用于反馈控制型工序步骤。
 */
public class PidController {

    private final double kp;
    private final double ki;
    private final double kd;

    private double integral;
    private double lastError;
    private boolean initialized;

    public PidController(double kp, double ki, double kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }

    public double compute(double target, double actual, double deltaSeconds) {
        double error = target - actual;
        if (!initialized) {
            lastError = error;
            initialized = true;
        }
        integral += error * deltaSeconds;
        double derivative = deltaSeconds > 0D ? (error - lastError) / deltaSeconds : 0D;
        lastError = error;
        return kp * error + ki * integral + kd * derivative;
    }

    public void reset() {
        integral = 0D;
        lastError = 0D;
        initialized = false;
    }
}
