/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.sync.manager.pull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.line.feign.TFFeign;
import com.ourexists.mesedge.line.model.TFVo;
import com.ourexists.mesedge.line.pojo.Line;
import com.ourexists.mesedge.line.service.LineService;
import com.ourexists.mesedge.mo.enums.MoSourceEnum;
import com.ourexists.mesedge.mo.enums.MoSplitEnum;
import com.ourexists.mesedge.mo.feign.MOFeign;
import com.ourexists.mesedge.mo.model.MODetailDto;
import com.ourexists.mesedge.mo.model.MODto;
import com.ourexists.mesedge.mo.model.MOTFDto;
import com.ourexists.mesedge.mps.feign.MPSFeign;
import com.ourexists.mesedge.mps.model.MPSFlowDto;
import com.ourexists.mesedge.mps.model.MPSTFDto;
import com.ourexists.mesedge.portal.flow.MpsFlowManager;
import com.ourexists.mesedge.portal.sync.manager.AbstractSyncFlow;
import com.ourexists.mesedge.portal.sync.manager.SyncFlow;
import com.ourexists.mesedge.portal.sync.manager.SyncManager;
import com.ourexists.mesedge.portal.sync.manager.Transfer;
import com.ourexists.mesedge.portal.third.YGApi;
import com.ourexists.mesedge.portal.third.model.resp.Order;
import com.ourexists.mesedge.sync.enums.SyncTxEnum;
import com.ourexists.mesedge.sync.pojo.Sync;
import com.ourexists.mesedge.sync.service.SyncResourceService;
import com.ourexists.mesedge.sync.service.SyncService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MoPullSyncManager extends SyncManager {

    @Autowired
    private MOFeign moFeign;

    @Autowired
    private MpsFlowManager mpsFlowManager;

    @Autowired
    private TFFeign tfFeign;

    @Autowired
    private MPSFeign mpsFeign;

    @Autowired
    private LineService lineService;

    @Autowired
    private YGApi ygApi;

    public MoPullSyncManager(SyncService syncService, SyncResourceService syncResourceService) {
        super(syncService, syncResourceService);
    }

    @Override
    public String syncTx() {
        return SyncTxEnum.MO_PULL.name();
    }

    @Override
    protected List<SyncFlow> flows() {
        List<SyncFlow> r = new ArrayList<>();
        r.add(new AbstractSyncFlow(syncResourceService) {
            @Override
            public String point() {
                return "RmoteReq";
            }

            @Override
            public int sort() {
                return 0;
            }

            @Override
            protected void doSync(Transfer transfer) {
                //远程请求订单接口
                List<Order> orders = ygApi.selectOrder(transfer.getStartTime(), transfer.getEndTime());
                //处理数据启停区间
                if (CollectionUtil.isBlank(orders)) {
                    return;
                }

                List<Order> enabled;
                BigInteger minId = null;
                BigInteger maxId = null;

                enabled = new ArrayList<>();
                for (Order order : orders) {
                    BigInteger id = new BigInteger(order.getId());
                    if (minId == null) {
                        minId = id;
                    } else {
                        if (id.compareTo(minId) < 0) {
                            minId = id;
                        }
                    }
                    if (maxId == null) {
                        maxId = id;
                    } else {
                        if (id.compareTo(maxId) > 0) {
                            maxId = id;
                        }
                    }
                    if (StringUtils.isNotEmpty(transfer.getLastPartMax())) {
                        BigInteger last = new BigInteger(transfer.getLastPartMax());
                        if (id.compareTo(last) > 0) {
                            enabled.add(order);
                        }
                    } else {
                        enabled.add(order);
                    }
                }
                //更新当前分片数据区间
                syncService.update(new LambdaUpdateWrapper<Sync>().set(Sync::getPartMin, minId).set(Sync::getPartMax, maxId).eq(Sync::getId, transfer.getSyncId()));
                //请求结果存入data
                transfer.setJsonData(JSON.toJSONString(enabled));
            }
        });
        r.add(new AbstractSyncFlow(syncResourceService) {
            @Override
            public String point() {
                return "CreateMO";
            }

            @Override
            public int sort() {
                return 1;
            }

            @Override
            public void doSync(Transfer transfer) {
                if (StringUtils.isBlank(transfer.getJsonData())) {
                    return;
                }
                //数据处理转换
                List<Order> orders = JSON.parseArray(transfer.getJsonData(), Order.class);
                if (CollectionUtil.isBlank(orders)) {
                    return;
                }
                List<MODto> dtos = new ArrayList<>();
                List<MPSFlowDto> mpsFlowDtos = new ArrayList<>();

                for (Order order : orders) {
                    if (StringUtils.isBlank(order.getProcessRoute())) {
                        continue;
                    }
                    String mcode = "Auto_" + IdWorker.getIdStr();
                    MODto moDto = new MODto()
                            .setProductCode(mcode)
                            .setProductName(mcode)
                            .setSelfCode(order.getPlanFrameCode())
                            .setLineCode(order.getProcessRoute())
                            .setSource(MoSourceEnum.MSE.getCode())
                            .setSourceId(order.getPlanFrameCode())
                            .setExecTime(order.getPlanStartTime())
                            .setNum(1)
                            .setSurplus(1);

                    Line line = lineService.selectByCode(order.getProcessRoute());
                    if (line == null) {
                        throw new BusinessException("lock of Line[" + order.getProcessRoute() + "]");
                    }
                    List<TFVo> tfs = null;
                    try {
                        tfs = RemoteHandleUtils.getDataFormResponse(tfFeign.selectByLineId(line.getId()));
                    } catch (EraCommonException e) {
                        throw new BusinessException(e.getMessage());
                    }
                    List<MOTFDto> tfDtoList = new ArrayList<>();
                    List<MPSTFDto> mpstfDtos = new ArrayList<>();
                    for (TFVo tf : tfs) {
                        MOTFDto tfDto = new MOTFDto();
                        MPSTFDto mpsDto = new MPSTFDto();
                        BeanUtils.copyProperties(tf, tfDto);
                        BeanUtils.copyProperties(tf, mpsDto);
                        tfDtoList.add(tfDto);
                        mpstfDtos.add(mpsDto);
                    }
                    List<MODetailDto> detailDtoList = new ArrayList<>();
                    for (String s : order.getBarCodeList()) {
                        detailDtoList.add(new MODetailDto()
                                .setMcode(mcode)
                                .setMatName(s)
                                .setMatCode(s)
                                .setMatNum(BigDecimal.ONE)
                                .setMatId(s));
                    }
                    moDto
                            .setDetailDtoList(detailDtoList)
                            .setTfDtoList(tfDtoList);
                    dtos.add(moDto);

                    MPSFlowDto flowDto = new MPSFlowDto()
                            .setMoCode(moDto.getSelfCode())
                            .setId(moDto.getId())
                            .setLine(moDto.getLineCode())
                            .setTfs(mpstfDtos)
                            .setSequence(order.getPriority())
                            .setExecTime(order.getPlanStartTime())
                            .setExecType(MoSplitEnum.disposable.getCode());
                    mpsFlowDtos.add(flowDto);
                }
                try {
                    RemoteHandleUtils.getDataFormResponse(moFeign.addBatch(dtos));
                } catch (EraCommonException e) {
                    throw new BusinessException(e.getMessage());
                }
                transfer.setJsonData(JSON.toJSONString(mpsFlowDtos));
            }
        });
        r.add(new AbstractSyncFlow(syncResourceService) {
            @Override
            public String point() {
                return "GenMPS";
            }

            @Override
            public int sort() {
                return 2;
            }

            @Override
            public void doSync(Transfer transfer) {
                if (StringUtils.isBlank(transfer.getJsonData())) {
                    return;
                }
                List<MPSFlowDto> flowDtos = JSONArray.parseArray(transfer.getJsonData(), MPSFlowDto.class);
                if (CollectionUtil.isBlank(flowDtos)) {
                    return;
                }
                mpsFlowManager.doFlowBatch(flowDtos);
                List<String> moCodes = flowDtos.stream().map(MPSFlowDto::getMoCode).collect(Collectors.toList());
                transfer.setJsonData(JSON.toJSONString(moCodes));
            }
        });
        r.add(new AbstractSyncFlow(syncResourceService) {
            @Override
            public String point() {
                return "JoinQue";
            }

            @Override
            public int sort() {
                return 3;
            }

            @Override
            public void doSync(Transfer transfer) {
                if (StringUtils.isBlank(transfer.getJsonData())) {
                    return;
                }
                List<String> moCodes = JSONArray.parseArray(transfer.getJsonData(), String.class);
                if (CollectionUtil.isBlank(moCodes)) {
                    return;
                }
                try {
                    RemoteHandleUtils.getDataFormResponse(mpsFeign.joinQueueBatchByMoCodesLimitEnable(moCodes));
                } catch (EraCommonException e) {
                    throw new BusinessException(e.getMessage());
                }
            }
        });
        return r;
    }
}
