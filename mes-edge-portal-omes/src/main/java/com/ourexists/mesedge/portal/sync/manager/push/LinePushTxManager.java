/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.sync.manager.push;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.era.txflow.*;
import com.ourexists.mesedge.line.feign.LineFeign;
import com.ourexists.mesedge.line.model.LineVo;
import com.ourexists.mesedge.line.model.TFVo;
import com.ourexists.mesedge.portal.s7.ocpua.OpcUaContext;
import com.ourexists.mesedge.sync.enums.SyncTxEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class LinePushTxManager extends TxManager {

    @Autowired
    private OpcUaContext opcUaContext;

    @Autowired
    private LineFeign lineFeign;

    public LinePushTxManager(TxStore txStore) {
        super(txStore);
    }

    @Override
    public String txName() {
        return SyncTxEnum.LINE_PUSH.name();
    }

    @Override
    protected List<TxBranchFlow> flows() {
        List<TxBranchFlow> r = new ArrayList<>();
        r.add(new AbstractTxBranchFlow(txStore) {
            @Override
            public String point() {
                return "Query_TFS";
            }

            @Override
            public int sort() {
                return 0;
            }

            @Override
            public void doExec(TxTransfer txTransfer) {
                JSONObject jo = JSON.parseObject(txTransfer.getJsonData());
                String lineId = jo.getString("lineId");
                LineVo lineVo;
                try {
                    lineVo = RemoteHandleUtils.getDataFormResponse(lineFeign.selectById(lineId));
                } catch (EraCommonException e) {
                    log.error(e.getMessage(), e);
                    return;
                }
                if (lineVo == null) {
                    return;
                }
                jo.put("lineVo", lineVo);
                txTransfer.setJsonData(jo.toJSONString());
            }
        });
        r.add(new AbstractTxBranchFlow(txStore) {
            @Override
            public String point() {
                return "write_line";
            }

            @Override
            public int sort() {
                return 1;
            }

            @Override
            public void doExec(TxTransfer txTransfer) {
                JSONObject jo = JSON.parseObject(txTransfer.getJsonData());
                LineVo lineVo = jo.getObject("lineVo", LineVo.class);
                if (lineVo.getMapDb() == null || lineVo.getMapDb() == -1 || StringUtils.isBlank(lineVo.getMapOffset())) {
                    return;
                }
                String serverName = jo.getString("serverName");
                List<TFVo> tfs = lineVo.getTfs();
                Short[] fs = new Short[tfs.size()];
                for (int i = 0; i < tfs.size(); i++) {
                    fs[i] = Short.parseShort(tfs.get(i).getSelfCode());
                }
                try {
                    opcUaContext.writeNodeValue(serverName, lineVo.getMapDb(), lineVo.getMapOffset(), fs);
                } catch (Exception e) {
                    log.error("write line error", e);
                    throw new BusinessException("write line error");
                }
            }
        });
        r.add(new AbstractTxBranchFlow(txStore) {
            @Override
            public String point() {
                return "write_flow";
            }

            @Override
            public int sort() {
                return 2;
            }

            @Override
            public void doExec(TxTransfer txTransfer) {
                JSONObject jo = JSON.parseObject(txTransfer.getJsonData());
                LineVo lineVo = jo.getObject("lineVo", LineVo.class);
                String serverName = jo.getString("serverName");
                txTransfer.setJsonData(lineVo.getId());
                List<TFVo> tfs = lineVo.getTfs();
                if (CollectionUtil.isBlank(tfs)) {
                    return;
                }
                for (TFVo tf : tfs) {
                    if (tf.getMapDb() == null || tf.getMapDb() == -1 || StringUtils.isBlank(tf.getMapOffset())) {
                        continue;
                    }
                    long min = tf.getDuration() / 60;
                    long sec = tf.getDuration() % 60;
                    try {
                        opcUaContext.writeNodeValue(serverName, tf.getMapDb(), tf.getMapOffset() + ".\"minSet\"", (short) min);
                        opcUaContext.writeNodeValue(serverName, tf.getMapDb(), tf.getMapOffset() + ".\"secSet\"", (short) sec);
                        opcUaContext.writeNodeValue(serverName, tf.getMapDb(), tf.getMapOffset() + ".\"tempSet\"", tf.getTemperature().floatValue());
                    } catch (Exception e) {
                        log.error("write_flow exception", e);
                        throw new BusinessException("write [" + tf.getName() + "] error");
                    }
                }

            }
        });
        r.add(new AbstractTxBranchFlow(txStore) {
            @Override
            public String point() {
                return "update_S7";
            }

            @Override
            public int sort() {
                return 3;
            }

            @Override
            public void doExec(TxTransfer txTransfer) {
                try {
                    RemoteHandleUtils.getDataFormResponse(lineFeign.updatePushTime(txTransfer.getJsonData()));
                } catch (EraCommonException e) {
                    throw new BusinessException(e.getMessage());
                }
            }
        });
        return r;
    }
}
