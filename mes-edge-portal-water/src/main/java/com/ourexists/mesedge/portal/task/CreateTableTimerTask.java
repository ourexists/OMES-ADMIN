/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task;

import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("CreateTable")
public class CreateTableTimerTask extends TimerTask {

    @Autowired
    private FunMapper funMapper;

    @Override
    public void doRun() {
        funMapper.createTableMonth("t_collect_wincc_datalist", 1);
        funMapper.createTableMonth("t_wincc_dosing_dev", 1);
        funMapper.createTableMonth("t_wincc_mag_dev", 1);
        funMapper.createTableMonth("t_wincc_od11_dev", 1);
        funMapper.createTableMonth("t_wincc_od12_dev", 1);
        funMapper.createTableMonth("t_wincc_od20_dev", 1);
        funMapper.createTableMonth("t_wincc_wps_dev", 1);
    }
}
