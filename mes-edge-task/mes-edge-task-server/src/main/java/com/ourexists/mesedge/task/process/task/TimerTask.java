/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.task.process.task;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class TimerTask implements Runnable {

    @Override
    public void run() {
        log.debug("task[{}] process start", this.getClass().getSimpleName());
        doRun();
        log.debug("task[{}] process end", this.getClass().getSimpleName());
    }

    public abstract void doRun();

    public String getName() {
        return this.getClass().getSimpleName();
    }
}
