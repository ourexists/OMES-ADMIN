/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.sync.manager.pull;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.era.txflow.*;
import com.ourexists.omes.line.feign.TFFeign;
import com.ourexists.omes.line.feign.TFEdgeFeign;
import com.ourexists.omes.line.model.TFVo;
import com.ourexists.omes.line.model.TFEdgeVo;
import com.ourexists.omes.line.pojo.Line;
import com.ourexists.omes.line.service.LineService;
import com.ourexists.omes.mo.enums.MoSourceEnum;
import com.ourexists.omes.mo.enums.MoSplitEnum;
import com.ourexists.omes.mo.feign.MOFeign;
import com.ourexists.omes.mo.model.MODetailDto;
import com.ourexists.omes.mo.model.MODto;
import com.ourexists.omes.mo.model.MOTFDto;
import com.ourexists.omes.mps.feign.MPSFeign;
import com.ourexists.omes.mps.model.MPSFlowDto;
import com.ourexists.omes.mps.model.MPSTFDto;
import com.ourexists.omes.portal.flow.MpsFlowManager;
import com.ourexists.omes.portal.third.YGApi;
import com.ourexists.omes.portal.third.model.resp.Order;
import com.ourexists.omes.sync.enums.SyncTxEnum;
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
public class MoPullTxManager extends TxManager {

    @Autowired
    private MOFeign moFeign;

    @Autowired
    private MpsFlowManager mpsFlowManager;

    @Autowired
    private TFFeign tfFeign;

    @Autowired
    private TFEdgeFeign tfEdgeFeign;

    @Autowired
    private MPSFeign mpsFeign;

    @Autowired
    private LineService lineService;

    @Autowired
    private YGApi ygApi;

    public MoPullTxManager(TxStore txStore) {
        super(txStore);
    }

    @Override
    public String txName() {
        return SyncTxEnum.MO_PULL.name();
    }

    @Override
    protected List<TxBranchFlow> flows() {
        List<TxBranchFlow> r = new ArrayList<>();
        r.add(new AbstractTxBranchFlow(txStore) {
            @Override
            public String point() {
                return "RmoteReq";
            }

            @Override
            public int sort() {
                return 0;
            }

            @Override
            protected void doExec(TxTransfer txTransfer) {
                //远程请求订单接口
                List<Order> orders = ygApi.selectOrder(txTransfer.getTx().getPartStartTimestamp(), txTransfer.getTx().getPartEndTimestamp());
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
                    if (StringUtils.isNotEmpty(txTransfer.getTx().getPreMax())) {
                        BigInteger last = new BigInteger(txTransfer.getTx().getPreMax());
                        if (id.compareTo(last) > 0) {
                            enabled.add(order);
                        }
                    } else {
                        enabled.add(order);
                    }
                }
                //请求结果存入data
                txTransfer.setJsonData(JSON.toJSONString(enabled));
                txTransfer.getTx()
                        .setPartMin(minId.toString())
                        .setPartMax(maxId.toString());
            }
        });
        r.add(new AbstractTxBranchFlow(txStore) {
            @Override
            public String point() {
                return "CreateMO";
            }

            @Override
            public int sort() {
                return 1;
            }

            @Override
            public void doExec(TxTransfer txTransfer) {
                if (StringUtils.isBlank(txTransfer.getJsonData())) {
                    return;
                }
                //数据处理转换
                List<Order> orders = JSON.parseArray(txTransfer.getJsonData(), Order.class);
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

                    // 根据 TF 边关系，为每个工序计算前置工序列表（并行：需要等待所有前置完成）
                    List<TFEdgeVo> tfEdges = null;
                    try {
                        tfEdges = RemoteHandleUtils.getDataFormResponse(tfEdgeFeign.selectByLineId(line.getId()));
                    } catch (EraCommonException e) {
                        throw new BusinessException(e.getMessage());
                    }

                    if (CollectionUtil.isNotBlank(tfs)) {
                        if (CollectionUtil.isNotBlank(tfEdges)) {
                            // 边关系存在：按边计算前置集合（并行：等待所有前置完成）
                            java.util.Map<String, String> idToSelfCode = new java.util.HashMap<>();
                            for (TFVo tf : tfs) {
                                idToSelfCode.put(tf.getId(), tf.getSelfCode());
                            }
                            java.util.Map<String, java.util.List<String>> toPreCodes = new java.util.HashMap<>();
                            for (TFEdgeVo edge : tfEdges) {
                                if (edge == null) continue;
                                String fromCode = idToSelfCode.get(edge.getFromTfId());
                                if (fromCode == null) continue;
                                String toTfId = edge.getToTfId();
                                if (toTfId == null) continue;
                                toPreCodes.computeIfAbsent(toTfId, k -> new java.util.ArrayList<>()).add(fromCode);
                            }
                            for (TFVo tf : tfs) {
                                java.util.List<String> preCodes = toPreCodes.get(tf.getId());
                                if (CollectionUtil.isNotBlank(preCodes)) {
                                    tf.setPre(String.join(",", preCodes));
                                }
                            }
                        } else {
                            // 兼容旧数据：关系表为空时，按优先级线性顺序生成前置（与旧 rowDrag 语义保持一致）
                            if (!tfs.isEmpty()) {
                                tfs.get(0).setPre(null);
                            }
                            for (int i = 1; i < tfs.size(); i++) {
                                TFVo cur = tfs.get(i);
                                TFVo pre = tfs.get(i - 1);
                                if (cur != null && pre != null) {
                                    cur.setPre(pre.getSelfCode());
                                }
                            }
                        }
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
                txTransfer.setJsonData(JSON.toJSONString(mpsFlowDtos));
            }
        });
        r.add(new AbstractTxBranchFlow(txStore) {
            @Override
            public String point() {
                return "GenMPS";
            }

            @Override
            public int sort() {
                return 2;
            }

            @Override
            public void doExec(TxTransfer txTransfer) {
                if (StringUtils.isBlank(txTransfer.getJsonData())) {
                    return;
                }
                List<MPSFlowDto> flowDtos = JSONArray.parseArray(txTransfer.getJsonData(), MPSFlowDto.class);
                if (CollectionUtil.isBlank(flowDtos)) {
                    return;
                }
                mpsFlowManager.doFlowBatch(flowDtos);
                List<String> moCodes = flowDtos.stream().map(MPSFlowDto::getMoCode).collect(Collectors.toList());
                txTransfer.setJsonData(JSON.toJSONString(moCodes));
            }
        });
        r.add(new AbstractTxBranchFlow(txStore) {
            @Override
            public String point() {
                return "JoinQue";
            }

            @Override
            public int sort() {
                return 3;
            }

            @Override
            public void doExec(TxTransfer txTransfer) {
                if (StringUtils.isBlank(txTransfer.getJsonData())) {
                    return;
                }
                List<String> moCodes = JSONArray.parseArray(txTransfer.getJsonData(), String.class);
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
