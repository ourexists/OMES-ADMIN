package com.ourexists.omes.portal.task;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.core.workshop.collect.WorkshopRealtimeCollectSelector;
import com.ourexists.omes.device.core.workshop.collect.WorkshopRealtimeCollector;
import com.ourexists.omes.portal.third.wincc.WinccApi;
import com.ourexists.omes.sync.enums.ProtocolEnum;
import com.ourexists.omes.sync.feign.ConnectFeign;
import com.ourexists.omes.sync.model.ConnectDto;
import com.ourexists.omes.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * wincc 采集器
 */
@Slf4j
@Component("WinccCollect")
public class WinccCollectTimerTask extends TimerTask {

    @Autowired
    private WinccApi winccApi;

    @Autowired
    private ConnectFeign connectFeign;

    @Autowired
    private WorkshopRealtimeCollectSelector selector;

    @Override
    public void doRun() {
        UserContext.defaultTenant();
        //获取连接器
        List<ConnectDto> connects = connect();
        if (CollectionUtils.isEmpty(connects)) {
            return;
        }
        for (ConnectDto connect : connects) {
            //根据不同租户调整
            UserContext.getTenant().setTenantId(connect.getTenantId());
            WorkshopRealtimeCollector collector = selector.getCollector(connect.getServerName());
            List<String> varis = collector.collectVariables();
            if (CollectionUtils.isEmpty(varis)) {
                return;
            }
            Map<String, Object> result = winccApi.pullTags(connect, varis);
            collector.doCollect(result);
        }

    }


    private List<ConnectDto> connect() {
        try {
            return RemoteHandleUtils.getDataFormResponse(connectFeign.selectConnectByProtocol(ProtocolEnum.WINCC.name()));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
    }
}
