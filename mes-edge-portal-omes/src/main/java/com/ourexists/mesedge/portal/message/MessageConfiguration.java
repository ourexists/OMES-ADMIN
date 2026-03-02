package com.ourexists.mesedge.portal.message;

import com.alibaba.fastjson2.JSONArray;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.TenantInfo;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.device.feign.WorkshopFeign;
import com.ourexists.mesedge.device.model.WorkshopAssignDto;
import com.ourexists.mesedge.message.core.NotifyMsg;
import com.ourexists.mesedge.message.enums.MessageSourceEnum;
import com.ourexists.mesedge.message.feign.MessageFeign;
import com.ourexists.mesedge.message.model.MessageDto;
import com.ourexists.mesedge.ucenter.account.AccPageQuery;
import com.ourexists.mesedge.ucenter.account.AccVo;
import com.ourexists.mesedge.ucenter.feign.AccountFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class MessageConfiguration {

    @Autowired
    private MessageFeign messageFeign;

//    @Autowired
//    private SinkManager sinkManager;

    @Autowired
    private AccountFeign accountFeign;

    @Autowired
    private WorkshopFeign workshopFeign;

    @Bean
    public MessageChannel notifyInputChannel() {
        return new QueueChannel(100);
    }

    @Bean
    public GenericHandler<String> genericHandler() {
        return (payload, headers) -> {
            List<NotifyMsg> notifyMsgs = JSONArray.parseArray(payload, NotifyMsg.class);
            for (NotifyMsg notifyMsg : notifyMsgs) {
                MessageDto dto = new MessageDto();
                BeanUtils.copyProperties(notifyMsg, dto, "id");
                dto.setNotifyId(notifyMsg.getId());
                UserContext.setTenant(new TenantInfo().setTenantId(notifyMsg.getTenantId()));
                try {
                    List<String> accIds = null;
                    if (MessageSourceEnum.System.name().equals(notifyMsg.getSource())) {
                        AccPageQuery accPageQuery = new AccPageQuery();
                        accPageQuery.setRequirePage(false);
                        accPageQuery.setPlatform(notifyMsg.getPlatform());
                        accPageQuery.setTenantId(notifyMsg.getTenantId());
                        List<AccVo> accVos = RemoteHandleUtils.getDataFormResponse(accountFeign.selectByPage(accPageQuery));
                        if (!CollectionUtils.isEmpty(accVos)) {
                            accIds = accVos.stream().map(AccVo::getId).collect(Collectors.toList());
                        }
                    } else if (MessageSourceEnum.Equip.name().equals(notifyMsg.getSource())) {
                        List<WorkshopAssignDto> workshopAssignDtos =
                                RemoteHandleUtils.getDataFormResponse(workshopFeign.selectWorkshopAssignByEquipId(notifyMsg.getSourceId()));
                        if (!CollectionUtils.isEmpty(workshopAssignDtos)) {
                            List<String> roleIds = workshopAssignDtos.stream().map(WorkshopAssignDto::getAssignId).collect(Collectors.toList());
                            List<AccVo> accVos = RemoteHandleUtils.getDataFormResponse(accountFeign.selectByRoles(roleIds));
                            if (!CollectionUtils.isEmpty(accVos)) {
                                accIds = accVos.stream().map(AccVo::getId).collect(Collectors.toList());
                            }
                        }
                    }
                    dto.setSendAccounts(accIds);
                    RemoteHandleUtils.getDataFormResponse(messageFeign.produce(dto));
//                    if (messageVo == null) {
//                        return null;
//                    }
//                    sinkManager.sendToClient(notifyMsg.getPlatform(), JSON.toJSONString(messageVo));
                } catch (EraCommonException e) {
                    log.error(e.getMessage(), e);
                    return null;
                }
            }
            return null;
        };
    }

    @Bean
    public IntegrationFlow integrationFlow() {
        return IntegrationFlow.from(notifyInputChannel()).handle(genericHandler()).get();
    }
}
