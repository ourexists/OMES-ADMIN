/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.sync.manager;

import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.mesedge.sync.enums.SyncStatusEnum;
import com.ourexists.mesedge.sync.pojo.Sync;
import com.ourexists.mesedge.sync.pojo.SyncResource;
import com.ourexists.mesedge.sync.service.SyncResourceService;
import com.ourexists.mesedge.sync.service.SyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.*;

@Slf4j
public abstract class SyncManager {

    private List<SyncFlow> flows = new ArrayList<>();

    protected SyncService syncService;

    protected SyncResourceService syncResourceService;

    public SyncManager(SyncService syncService, SyncResourceService syncResourceService) {
        this.syncService = syncService;
        this.syncResourceService = syncResourceService;
        flows.addAll(flows());
        if (CollectionUtil.isBlank(flows)) {
            throw new RuntimeException("flows is empty");
        }
        flows.sort(Comparator.comparingInt(SyncFlow::sort));
    }

    public void execute(String initJsonData) {
        execute(new Date(), new Date(), initJsonData);
    }

    public void execute(Date startTime, Date endTime) {
        execute(startTime, endTime, null);
    }

    @Async
    public void execute(Date startTime, Date endTime, String initJsonData) {
        Sync last = syncService.selectLastSync(syncTx());
        Sync sync = new Sync()
                .setSyncTx(syncTx())
                .setPartStartTimestamp(startTime)
                .setPartEndTimestamp(endTime)
                .setStatus(SyncStatusEnum.start.name());
        Transfer transfer = new Transfer();
        transfer.setJsonData(initJsonData);
        if (last != null) {
            transfer.setLastPartMax(last.getPartMax());
            transfer.setLastPartMin(last.getPartMin());
            sync.setPreMax(last.getPartMax());
            sync.setPreMin(last.getPartMin());

            //基于上一个分片容错2分钟执行当前分片
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(last.getPartEndTimestamp());
            calendar.add(Calendar.MINUTE, -2);
            sync.setPartStartTimestamp(calendar.getTime());
        }
        syncService.save(sync);
        transfer
                .setSyncId(sync.getId())
                .setStartTime(sync.getPartStartTimestamp())
                .setEndTime(sync.getPartEndTimestamp());
        flowBreakExecute(transfer);
    }

    protected void flowBreakExecute(Transfer transfer) {
        flowBreakExecute(transfer, null, 0);
    }

    protected void flowBreakExecute(Transfer transfer, String point) {
        flowBreakExecute(transfer, point, 0);
    }

    protected void flowBreakExecute(Transfer transfer, String point, int offset) {

        if (CollectionUtil.isNotBlank(flows)) {
            int pos = -1;
            if (point == null) {
                pos = offset;
            }
            try {
                for (int i = 0; i < flows.size(); i++) {
                    if (pos < 0 && flows.get(i).point().equals(point)) {
                        pos = i + offset;
                    }
                }
                //偏移位置大于总流程说明完成了
                if (pos < flows.size()) {
                    for (int i = 0; i < flows.size(); i++) {
                        if (i >= pos) {
                            flows.get(i).sync(transfer);
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                syncService.updateStatus(transfer.getSyncId(), SyncStatusEnum.error);
                return;
            }
        }
        syncService.updateStatus(transfer.getSyncId(), SyncStatusEnum.end);
    }

    public abstract String syncTx();

    protected abstract List<SyncFlow> flows();

    /**
     * 流程断点处理
     */
    public void breakpointProcess(Sync sync) {
        //已经完成的说明已经异常处理完了
        if (sync.getStatus().equals(SyncStatusEnum.end.name())) {
            return;
        }

        SyncResource syncResource = syncResourceService.getLastFlow(sync.getId());
        //说明流程刚开始，不关注
        if (syncResource == null) {
            return;
        }
        SyncStatusEnum flowStatus = SyncStatusEnum.valueOf(syncResource.getStatus());
        switch (flowStatus) {
            case start:
                //正常进行中
                return;
            case error:
                Transfer transfer = new Transfer()
                        .setSyncId(syncResource.getSyncId())
                        .setJsonData(syncResource.getReqData())
                        .setStartTime(sync.getPartStartTimestamp())
                        .setEndTime(sync.getPartEndTimestamp())
                        .setLastPartMin(sync.getPreMin())
                        .setLastPartMax(sync.getPreMax());
                //产生异常了,从异常处开始执行
                flowBreakExecute(transfer, syncResource.getPoint());
                break;
            case end:
                Transfer transfer2 = new Transfer()
                        .setSyncId(syncResource.getSyncId())
                        .setJsonData(syncResource.getRespData())
                        .setStartTime(sync.getPartStartTimestamp())
                        .setEndTime(sync.getPartEndTimestamp())
                        .setLastPartMin(sync.getPreMin())
                        .setLastPartMax(sync.getPreMax());
                //同步状态进行中而最后的流程为完成态,携带结束参数从下一流程点开始执行
                flowBreakExecute(transfer2, syncResource.getPoint(), 1);
                break;
        }
    }
}
