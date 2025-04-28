package com.ourexists.mesedge.portal.task; ///*
// * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
// */
//
//package com.ourexists.mesedge.task.process.task;
//
//import com.ourexists.era.framework.core.utils.CollectionUtil;
//import com.ourexists.mesedge.model.enums.MPSStatusEnum;
//import com.ourexists.mesedge.mps.pojo.MPS;
//import com.ourexists.mesedge.mps.service.MPSService;
//import com.ourexists.mesedge.task.process.ExecQueue;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Component("MpsExecing")
//public class MpsExecingTimerTask extends TimerTask {
//
//    @Autowired
//    private MPSService mpsService;
//
//    @Override
//    public void doRun() {
//        List<MPS> mpsList = mpsService.selectByStatus(MPSStatusEnum.EXECING);
//        if (CollectionUtil.isBlank(mpsList)) {
//            return;
//        }
//        ExecQueue.joinAll(mpsList.stream().map(MPS::getId).collect(Collectors.toList()));
//    }
//}
