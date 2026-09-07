/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.task.process.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

@Slf4j
public abstract class TimerTask implements Runnable {

    @Override
    public void run() {
        log.debug("task[{}] process start", this.getClass().getSimpleName());
        try {
            doRun();
        } catch (CannotGetJdbcConnectionException e) {
            if (isDataSourceClosed(e)) {
                log.warn("task[{}] skipped because datasource already closed during shutdown", this.getClass().getSimpleName());
            } else {
                log.error("task[{}] process error", this.getClass().getSimpleName(), e);
            }
        } catch (Throwable e) {
            if (isDataSourceClosed(e)) {
                log.warn("task[{}] skipped because datasource already closed during shutdown", this.getClass().getSimpleName());
            } else {
                log.error("task[{}] process error", this.getClass().getSimpleName(), e);
            }
        }
        log.debug("task[{}] process end", this.getClass().getSimpleName());
    }

    public abstract void doRun();

    public String getName() {
        return this.getClass().getSimpleName();
    }

    private boolean isDataSourceClosed(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String msg = current.getMessage();
            if (msg != null && msg.contains("HikariDataSource") && msg.contains("has been closed")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
