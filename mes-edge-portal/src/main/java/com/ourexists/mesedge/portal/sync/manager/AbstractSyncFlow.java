/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.sync.manager;

import com.ourexists.mesedge.sync.enums.SyncStatusEnum;
import com.ourexists.mesedge.sync.pojo.SyncResource;
import com.ourexists.mesedge.sync.service.SyncResourceService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSyncFlow implements SyncFlow {

    private SyncResourceService syncResourceService;

    public AbstractSyncFlow(SyncResourceService syncResourceService) {
        this.syncResourceService = syncResourceService;
    }

    @Override
    public void sync(Transfer transfer) {
        String point = point();
        SyncResource syncResource = new SyncResource()
                .setSyncId(transfer.getSyncId())
                .setPoint(point)
                .setStatus(SyncStatusEnum.start.name())
                .setReqData(transfer.getJsonData());
        syncResourceService.save(syncResource);
        try {
            doSync(transfer);
        } catch (Exception e) {
            log.error("", e);
            syncResourceService.updateStatus(syncResource.getId(), SyncStatusEnum.error, null, e.getMessage());
            throw e;
        }
        syncResourceService.updateStatus(syncResource.getId(), SyncStatusEnum.end, transfer.getJsonData(), null);
    }

    protected abstract void doSync(Transfer transfer);

}
