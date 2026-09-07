/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.flow;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.line.feign.LineFeign;
import com.ourexists.omes.line.feign.TFEdgeFeign;
import com.ourexists.omes.line.feign.TFFeign;
import com.ourexists.omes.line.model.LineVo;
import com.ourexists.omes.line.model.TFEdgeVo;
import com.ourexists.omes.line.model.TFVo;
import com.ourexists.omes.mo.enums.MOStatusEnum;
import com.ourexists.omes.mo.enums.MoAdjustLogStatusEnum;
import com.ourexists.omes.mo.enums.MoAdjustSourceEnum;
import com.ourexists.omes.mo.enums.MoAdjustTypeEnum;
import com.ourexists.omes.mo.enums.MoSplitEnum;
import com.ourexists.omes.mo.feign.MOFeign;
import com.ourexists.omes.mo.model.MODetailDto;
import com.ourexists.omes.mo.model.MODto;
import com.ourexists.omes.mo.model.MoAdjustCommand;
import com.ourexists.omes.mo.model.MoAdjustLogDto;
import com.ourexists.omes.mo.model.MoAdjustPreviewResult;
import com.ourexists.omes.mo.model.MoAdjustResult;
import com.ourexists.omes.mo.model.MoSurplusReconcileItem;
import com.ourexists.omes.mps.enums.MPSStatusEnum;
import com.ourexists.omes.mps.feign.MPSFeign;
import com.ourexists.omes.mps.model.ChangePriorityDto;
import com.ourexists.omes.mps.model.MPSFlowDetailDto;
import com.ourexists.omes.mps.model.MPSFlowDto;
import com.ourexists.omes.mps.model.MPSDto;
import com.ourexists.omes.mps.model.MPSTFDto;
import com.ourexists.omes.mps.model.MPSQueueOperateDto;
import com.ourexists.omes.portal.mps.model.MPSVo;
import com.ourexists.omes.portal.sync.manager.push.PlanAbortTxManager;
import com.ourexists.omes.portal.third.YGApi;
import com.ourexists.omes.sync.enums.SyncTxEnum;
import com.ourexists.omes.sync.feign.SyncFeign;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 生产订单调整编排（与 {@link MpsFlowManager} 同层）。
 * <pre>
 * | 类型        | WAIT_QUE | WAIT_EXEC       | EXECING              | COMPLETE/FILE |
 * | CANCEL_*    | void     | 退队+void       | 拒绝 / force 中止    | 保留/拒绝     |
 * | RESCHEDULE  | 改时间   | 可退 WAIT_QUE   | 拒绝                 | 拒绝          |
 * | PRIORITY    | —        | JUMP/REORDER    | 拒绝                 | 拒绝          |
 * | CHANGE_LINE | void重拆 | 同左            | 拒绝(P1 含 force)    | 保留原线      |
 * | CHANGE_DEV  | 改绑定   | 改绑定          | 拒绝                 | 拒绝          |
 * | QTY_UP      | 抬量     | —               | —                    | —             |
 * | QTY_DOWN    | void砍量 | void砍量        | 不可砍(非force)      | 不可砍        |
 * </pre>
 */
@Slf4j
@Component
public class MoAdjustOrchestrator {

    private static final List<Integer> ACTIVE_MPS_STATUSES = Arrays.asList(
            MPSStatusEnum.WAIT_QUE.getCode(),
            MPSStatusEnum.WAIT_EXEC.getCode(),
            MPSStatusEnum.EXECING.getCode(),
            MPSStatusEnum.COMPLETE.getCode(),
            MPSStatusEnum.FILE.getCode()
    );

    private static final List<Integer> VOIDABLE_STATUSES = Arrays.asList(
            MPSStatusEnum.WAIT_QUE.getCode(),
            MPSStatusEnum.WAIT_EXEC.getCode()
    );

    private static final List<Integer> UNCANCELLABLE_STATUSES = Arrays.asList(
            MPSStatusEnum.EXECING.getCode(),
            MPSStatusEnum.COMPLETE.getCode(),
            MPSStatusEnum.FILE.getCode()
    );

    @Autowired
    private MOFeign moFeign;
    @Autowired
    private MPSFeign mpsFeign;
    @Autowired
    private MpsFlowManager mpsFlowManager;
    @Autowired
    private LineFeign lineFeign;
    @Autowired
    private TFFeign tfFeign;
    @Autowired
    private TFEdgeFeign tfEdgeFeign;
    @Autowired
    private MoLifecycleManager moLifecycleManager;
    @Autowired
    private SyncFeign syncFeign;
    @Autowired
    private YGApi ygApi;
    @Lazy
    @Autowired
    private PlanAbortTxManager planAbortTxManager;

    @Transactional(rollbackFor = Exception.class)
    public MoAdjustResult adjust(MoAdjustCommand command) {
        validateCommand(command);
        MoAdjustTypeEnum type = MoAdjustTypeEnum.of(command.getAdjustType());
        if (type == null) {
            throw new BusinessException("${mo.adjust.type.invalid}");
        }
        boolean force = Boolean.TRUE.equals(command.getForce());
        if (force) {
            requireForceOperator(command);
        }
        MoAdjustSourceEnum source = MoAdjustSourceEnum.of(command.getSource());
        command.setSource(source.name());
        command.setAdjustType(type.name());

        MoAdjustLogDto existing = feign(() -> moFeign.selectAdjustLogByRequestId(command.getRequestId()));
        if (existing != null) {
            if (MoAdjustLogStatusEnum.SUCCESS.getCode().equals(existing.getStatus())) {
                return replayResult(existing);
            }
            if (MoAdjustLogStatusEnum.PENDING.getCode().equals(existing.getStatus())) {
                throw new BusinessException("${mo.adjust.request.pending}");
            }
        }

        MODto mo = loadMo(command.getMoCode());
        if (MOStatusEnum.CANCEL.getCode().equals(mo.getStatus())
                && (type == MoAdjustTypeEnum.CANCEL_MO || type == MoAdjustTypeEnum.CANCEL_MPS)) {
            throw new BusinessException("${mo.adjust.already.cancel}");
        }

        List<MPSDto> allMps = loadMps(command.getMoCode());
        Map<String, Object> beforeSnap = snapshot(mo, allMps);

        MoAdjustLogDto logDto = existing != null ? existing : new MoAdjustLogDto();
        if (StringUtils.isBlank(logDto.getId())) {
            logDto.setId(IdWorker.getIdStr());
            logDto.setRequestId(command.getRequestId());
            logDto.setMoCode(command.getMoCode());
            logDto.setAdjustType(type.name());
            logDto.setSource(source.name());
            logDto.setOperator(command.getOperator());
            logDto.setCreateTime(new Date());
            logDto.setStatus(MoAdjustLogStatusEnum.PENDING.getCode());
            logDto.setBeforeJson(JSON.toJSONString(beforeSnap));
            feign(() -> moFeign.saveAdjustLog(logDto));
        } else {
            logDto.setAdjustType(type.name());
            logDto.setSource(source.name());
            logDto.setOperator(command.getOperator());
            logDto.setStatus(MoAdjustLogStatusEnum.PENDING.getCode());
            logDto.setErrMsg(null);
            logDto.setBeforeJson(JSON.toJSONString(beforeSnap));
            feign(() -> moFeign.updateAdjustLog(logDto));
        }

        MoAdjustResult result = new MoAdjustResult()
                .setLogId(logDto.getId())
                .setRequestId(command.getRequestId())
                .setMoCode(command.getMoCode())
                .setAdjustType(type.name());

        try {
            switch (type) {
                case CANCEL_MO -> doCancelMo(mo, allMps, force, result);
                case CANCEL_MPS -> doCancelMps(mo, allMps, command.getPayload(), force, result);
                case RESCHEDULE -> doReschedule(mo, allMps, command.getPayload(), result);
                case PRIORITY -> doPriority(mo, allMps, command.getPayload(), result);
                case CHANGE_LINE -> doChangeLine(mo, allMps, command.getPayload(), force, result);
                case CHANGE_DEV -> doChangeDev(mo, allMps, command.getPayload(), result);
                case QTY_UP -> doQtyUp(mo, allMps, command.getPayload(), result);
                case QTY_DOWN -> doQtyDown(mo, allMps, command.getPayload(), force, result);
                default -> throw new BusinessException("${mo.adjust.type.invalid}");
            }

            moLifecycleManager.recalc(command.getMoCode());
            MODto afterMo = loadMo(command.getMoCode());
            List<MPSDto> afterMps = loadMps(command.getMoCode());
            assertSurplus(afterMo, afterMps);

            result.setMo(afterMo);
            logDto.setAffectMpsIds(String.join(",", result.getAffectedMpsIds()));
            logDto.setAfterJson(JSON.toJSONString(snapshot(afterMo, afterMps)));
            logDto.setStatus(MoAdjustLogStatusEnum.SUCCESS.getCode());
            logDto.setErrMsg(null);
            feign(() -> moFeign.updateAdjustLog(logDto));
            return result;
        } catch (RuntimeException ex) {
            logDto.setStatus(MoAdjustLogStatusEnum.FAILED.getCode());
            logDto.setErrMsg(StringUtils.left(ex.getMessage(), 500));
            try {
                feign(() -> moFeign.updateAdjustLog(logDto));
            } catch (Exception ignore) {
                log.warn("failed to write adjust log failure status, requestId={}", command.getRequestId());
            }
            throw ex;
        }
    }

    public MoAdjustPreviewResult preview(MoAdjustCommand command) {
        validateCommand(command);
        MoAdjustTypeEnum type = MoAdjustTypeEnum.of(command.getAdjustType());
        if (type == null) {
            throw new BusinessException("${mo.adjust.type.invalid}");
        }
        boolean force = Boolean.TRUE.equals(command.getForce());
        MODto mo = loadMo(command.getMoCode());
        List<MPSDto> allMps = loadMps(command.getMoCode());
        MoAdjustPreviewResult preview = new MoAdjustPreviewResult()
                .setMoCode(mo.getSelfCode())
                .setAdjustType(type.name());
        try {
            switch (type) {
                case CANCEL_MO -> previewCancelMo(allMps, force, preview);
                case CANCEL_MPS -> previewCancelMps(mo, allMps, command.getPayload(), force, preview);
                case QTY_UP -> previewQtyUp(mo, command.getPayload(), preview);
                case QTY_DOWN -> previewQtyDown(mo, allMps, command.getPayload(), force, preview);
                case CHANGE_LINE -> previewChangeLine(allMps, command.getPayload(), force, preview);
                case CHANGE_DEV -> previewChangeDev(allMps, command.getPayload(), preview);
                case RESCHEDULE -> {
                    preview.getHints().add("will update execTime for WAIT_* batches");
                    preview.setWouldAffectMpsIds(allMps.stream()
                            .filter(m -> VOIDABLE_STATUSES.contains(m.getStatus()))
                            .map(MPSDto::getId).collect(Collectors.toList()));
                }
                case PRIORITY -> preview.getHints().add("will change queue priority for WAIT_EXEC");
                default -> throw new BusinessException("${mo.adjust.type.invalid}");
            }
        } catch (BusinessException ex) {
            preview.setAllowed(false);
            preview.setRejectReason(ex.getMessage());
        }
        return preview;
    }

    public List<MoAdjustLogDto> listLogs(String moCode) {
        if (StringUtils.isBlank(moCode)) {
            throw new BusinessException("${mo.adjust.moCode.required}");
        }
        return feign(() -> moFeign.selectAdjustLogsByMoCode(moCode));
    }

    public List<MoSurplusReconcileItem> reconcile(String moCode) {
        List<MODto> mos;
        if (StringUtils.isNotBlank(moCode)) {
            mos = Collections.singletonList(loadMo(moCode));
        } else {
            throw new BusinessException("${mo.adjust.moCode.required}");
        }
        List<MoSurplusReconcileItem> items = new ArrayList<>();
        for (MODto mo : mos) {
            List<MPSDto> mpsList = loadMps(mo.getSelfCode());
            int activeSum = sumActive(mpsList);
            int surplus = mo.getSurplus() == null ? 0 : mo.getSurplus();
            int num = mo.getNum() == null ? 0 : mo.getNum();
            MoSurplusReconcileItem item = new MoSurplusReconcileItem()
                    .setMoCode(mo.getSelfCode())
                    .setNum(num)
                    .setSurplus(surplus)
                    .setActiveMpsSum(activeSum)
                    .setExpectedSurplus(num - activeSum)
                    .setStatus(String.valueOf(mo.getStatus()));
            if (num != surplus + activeSum) {
                item.setMessage("INCONSISTENT: num != surplus + activeSum");
            } else {
                item.setMessage("OK");
            }
            items.add(item);
        }
        return items;
    }

    // ---------------- cancel ----------------

    private void doCancelMo(MODto mo, List<MPSDto> allMps, boolean force, MoAdjustResult result) {
        List<String> voidIds = collectVoidIds(allMps, null, force);
        maybeAbortMes(mo.getSelfCode(), voidIds, force, result);
        rollbackVoid(mo, voidIds, force, result);
        feign(() -> moFeign.updateStatus(Collections.singletonList(mo.getSelfCode()), MOStatusEnum.CANCEL));
        result.getAffectedMpsIds().addAll(result.getVoidedMpsIds());
    }

    private void doCancelMps(MODto mo, List<MPSDto> allMps, Map<String, Object> payload,
                             boolean force, MoAdjustResult result) {
        List<String> mpsIds = readStringList(payload, "mpsIds");
        if (CollectionUtil.isBlank(mpsIds)) {
            throw new BusinessException("${mo.adjust.mpsIds.required}");
        }
        Map<String, MPSDto> byId = indexById(allMps);
        for (String id : mpsIds) {
            MPSDto mps = byId.get(id);
            if (mps == null) {
                throw new BusinessException("${mo.adjust.mps.notfound}");
            }
            if (!mo.getSelfCode().equals(mps.getMoCode())) {
                throw new BusinessException("${mo.adjust.mps.mo.mismatch}");
            }
            if (UNCANCELLABLE_STATUSES.contains(mps.getStatus())
                    && !MPSStatusEnum.EXECING.getCode().equals(mps.getStatus())) {
                throw new BusinessException("${mps.msg.void.status}");
            }
            if (MPSStatusEnum.EXECING.getCode().equals(mps.getStatus()) && !force) {
                throw new BusinessException("${mo.adjust.execing.reject}");
            }
        }
        List<String> voidIds = collectVoidIds(allMps, mpsIds, force);
        maybeAbortMes(mo.getSelfCode(), voidIds, force, result);
        rollbackVoid(mo, voidIds, force, result);
        result.getAffectedMpsIds().addAll(result.getVoidedMpsIds());
    }

    private void previewCancelMo(List<MPSDto> allMps, boolean force, MoAdjustPreviewResult preview) {
        if (hasExecing(allMps, null) && !force) {
            preview.setAllowed(false);
            preview.setRequiresForce(true);
            preview.setRejectReason("${mo.adjust.execing.reject}");
            return;
        }
        preview.setWouldVoidMpsIds(collectVoidIds(allMps, null, force));
        preview.setSurplusDelta(sumNums(allMps, preview.getWouldVoidMpsIds()));
        if (force && hasExecing(allMps, null)) {
            preview.getWarnings().add("force will STOP EXECING TF and CANCEL MPS without MpsPush");
        }
    }

    private void previewCancelMps(MODto mo, List<MPSDto> allMps, Map<String, Object> payload,
                                  boolean force, MoAdjustPreviewResult preview) {
        List<String> mpsIds = readStringList(payload, "mpsIds");
        if (CollectionUtil.isBlank(mpsIds)) {
            preview.setAllowed(false);
            preview.setRejectReason("${mo.adjust.mpsIds.required}");
            return;
        }
        if (hasExecing(allMps, mpsIds) && !force) {
            preview.setAllowed(false);
            preview.setRequiresForce(true);
            preview.setRejectReason("${mo.adjust.execing.reject}");
            return;
        }
        preview.setWouldVoidMpsIds(collectVoidIds(allMps, mpsIds, force));
        preview.setSurplusDelta(sumNums(allMps, preview.getWouldVoidMpsIds()));
    }

    // ---------------- qty ----------------

    private void doQtyUp(MODto mo, List<MPSDto> allMps, Map<String, Object> payload, MoAdjustResult result) {
        int delta = readPositiveInt(payload, "delta", "Δnum");
        int oldNum = mo.getNum() == null ? 0 : mo.getNum();
        int oldSurplus = mo.getSurplus() == null ? 0 : mo.getSurplus();
        int newNum = oldNum + delta;
        int newSurplus = oldSurplus + delta;
        feign(() -> moFeign.updateNumAndSurplus(mo.getSelfCode(), newNum, newSurplus));
        mo.setNum(newNum);
        mo.setSurplus(newSurplus);
        result.setSurplusDelta(delta);
        result.getHints().add("QTY_UP done; call /flow/mps to split new surplus into MPS");
        assertSurplus(mo, allMps);
    }

    private void doQtyDown(MODto mo, List<MPSDto> allMps, Map<String, Object> payload,
                           boolean force, MoAdjustResult result) {
        Integer newNumObj = readInt(payload, "newNum");
        Integer deltaObj = readInt(payload, "delta");
        int oldNum = mo.getNum() == null ? 0 : mo.getNum();
        int newNum;
        if (newNumObj != null) {
            newNum = newNumObj;
        } else if (deltaObj != null) {
            newNum = oldNum - Math.abs(deltaObj);
        } else {
            throw new BusinessException("${mo.adjust.qty.required}");
        }
        if (newNum < 0) {
            throw new BusinessException("${mo.adjust.qty.invalid}");
        }
        int locked = sumByStatuses(allMps, UNCANCELLABLE_STATUSES);
        if (newNum < locked) {
            if (hasExecing(allMps, null) && !force) {
                throw new BusinessException("${mo.adjust.execing.reject}");
            }
            if (newNum < locked) {
                throw new BusinessException("${mo.adjust.qty.below.locked}");
            }
        }
        int needCut = oldNum - newNum;
        List<String> preferred = readStringList(payload, "mpsIds");
        List<MPSDto> candidates = allMps.stream()
                .filter(m -> VOIDABLE_STATUSES.contains(m.getStatus()))
                .sorted(Comparator.comparing((MPSDto m) -> m.getBatch() == null ? 0 : m.getBatch()).reversed())
                .collect(Collectors.toList());
        if (CollectionUtil.isNotBlank(preferred)) {
            Set<String> pref = new HashSet<>(preferred);
            candidates = candidates.stream().filter(m -> pref.contains(m.getId())).collect(Collectors.toList());
        }
        List<String> voidIds = new ArrayList<>();
        int cut = 0;
        for (MPSDto mps : candidates) {
            if (cut >= needCut) {
                break;
            }
            voidIds.add(mps.getId());
            cut += mps.getNum() == null ? 0 : mps.getNum();
        }
        if (cut < needCut) {
            throw new BusinessException("${mo.adjust.qty.not.enough.voidable}");
        }
        int oldSurplus = mo.getSurplus() == null ? 0 : mo.getSurplus();
        rollbackVoid(mo, voidIds, false, result);
        List<MPSDto> afterVoidView = allMps.stream()
                .filter(m -> !voidIds.contains(m.getId()))
                .collect(Collectors.toList());
        int activeAfter = sumActive(afterVoidView);
        int newSurplus = newNum - activeAfter;
        if (newSurplus < 0) {
            throw new BusinessException("${mo.adjust.qty.invalid}");
        }
        feign(() -> moFeign.updateNumAndSurplus(mo.getSelfCode(), newNum, newSurplus));
        mo.setNum(newNum);
        mo.setSurplus(newSurplus);
        result.setSurplusDelta(newSurplus - oldSurplus);
        result.getAffectedMpsIds().addAll(result.getVoidedMpsIds());
    }

    private void previewQtyUp(MODto mo, Map<String, Object> payload, MoAdjustPreviewResult preview) {
        int delta = readPositiveInt(payload, "delta", "Δnum");
        preview.setNewNum((mo.getNum() == null ? 0 : mo.getNum()) + delta);
        preview.setNewSurplus((mo.getSurplus() == null ? 0 : mo.getSurplus()) + delta);
        preview.setSurplusDelta(delta);
        preview.getHints().add("will not auto-split; call /flow/mps afterwards");
    }

    private void previewQtyDown(MODto mo, List<MPSDto> allMps, Map<String, Object> payload,
                                boolean force, MoAdjustPreviewResult preview) {
        Integer newNumObj = readInt(payload, "newNum");
        Integer deltaObj = readInt(payload, "delta");
        int oldNum = mo.getNum() == null ? 0 : mo.getNum();
        int newNum = newNumObj != null ? newNumObj : oldNum - Math.abs(deltaObj == null ? 0 : deltaObj);
        int locked = sumByStatuses(allMps, UNCANCELLABLE_STATUSES);
        if (newNum < locked) {
            preview.setAllowed(false);
            preview.setRequiresForce(hasExecing(allMps, null));
            preview.setRejectReason("${mo.adjust.qty.below.locked}");
            return;
        }
        preview.setNewNum(newNum);
        preview.getHints().add("will void WAIT_* by large batch first unless mpsIds specified");
    }

    // ---------------- change line / dev ----------------

    private void doChangeLine(MODto mo, List<MPSDto> allMps, Map<String, Object> payload,
                              boolean force, MoAdjustResult result) {
        // P1: EXECING 仍拒绝（含 force），避免半截在制品改线
        if (hasExecing(allMps, null)) {
            throw new BusinessException("${mo.adjust.change.line.execing}");
        }
        String newLine = firstNonBlank(stringVal(payload == null ? null : payload.get("newLineCode")),
                stringVal(payload == null ? null : payload.get("lineCode")));
        if (StringUtils.isBlank(newLine)) {
            throw new BusinessException("${mo.adjust.line.required}");
        }
        List<String> mpsIds = readStringList(payload, "mpsIds");
        List<String> voidIds = collectVoidIds(allMps, CollectionUtil.isBlank(mpsIds) ? null : mpsIds, false);
        int rebuildNum = sumNums(allMps, voidIds);
        rollbackVoid(mo, voidIds, false, result);
        feign(() -> moFeign.updateLineCode(mo.getSelfCode(), newLine));
        mo.setLineCode(newLine);
        if (rebuildNum > 0) {
            List<MPSVo> created = rebuildOnLine(mo, newLine, rebuildNum);
            if (CollectionUtil.isNotBlank(created)) {
                for (MPSVo vo : created) {
                    if (vo.getId() != null) {
                        result.getCreatedMpsIds().add(vo.getId());
                        result.getAffectedMpsIds().add(vo.getId());
                    }
                }
            }
            result.getHints().add("re-split " + rebuildNum + " on line " + newLine);
        }
        result.getAffectedMpsIds().addAll(result.getVoidedMpsIds());
    }

    private void previewChangeLine(List<MPSDto> allMps, Map<String, Object> payload,
                                   boolean force, MoAdjustPreviewResult preview) {
        if (hasExecing(allMps, null)) {
            preview.setAllowed(false);
            preview.setRejectReason("${mo.adjust.change.line.execing}");
            return;
        }
        List<String> mpsIds = readStringList(payload, "mpsIds");
        preview.setWouldVoidMpsIds(collectVoidIds(allMps, CollectionUtil.isBlank(mpsIds) ? null : mpsIds, false));
        preview.setSurplusDelta(0);
        preview.getHints().add("void WAIT_* then re-split on newLine via MpsFlowManager");
    }

    private void doChangeDev(MODto mo, List<MPSDto> allMps, Map<String, Object> payload, MoAdjustResult result) {
        if (payload == null) {
            throw new BusinessException("${mo.adjust.payload.required}");
        }
        List<Map<String, Object>> bindings = readMapList(payload, "bindings");
        if (CollectionUtil.isBlank(bindings)) {
            // single binding shorthand
            String mpsId = firstNonBlank(stringVal(payload.get("mpsId")), stringVal(payload.get("id")));
            String matCode = stringVal(payload.get("matCode"));
            String devNo = stringVal(payload.get("devNo"));
            String devName = stringVal(payload.get("devName"));
            if (StringUtils.isBlank(mpsId) || StringUtils.isBlank(matCode)) {
                throw new BusinessException("${mo.adjust.dev.payload}");
            }
            Map<String, Object> one = new HashMap<>();
            one.put("mpsId", mpsId);
            one.put("matCode", matCode);
            one.put("devNo", devNo);
            one.put("devName", devName);
            bindings = Collections.singletonList(one);
        }
        Map<String, MPSDto> byId = indexById(allMps);
        for (Map<String, Object> b : bindings) {
            String mpsId = stringVal(b.get("mpsId"));
            String matCode = stringVal(b.get("matCode"));
            MPSDto mps = byId.get(mpsId);
            if (mps == null) {
                throw new BusinessException("${mo.adjust.mps.notfound}");
            }
            if (!mo.getSelfCode().equals(mps.getMoCode())) {
                throw new BusinessException("${mo.adjust.mps.mo.mismatch}");
            }
            if (!VOIDABLE_STATUSES.contains(mps.getStatus())) {
                throw new BusinessException("${mo.adjust.dev.status}");
            }
            feign(() -> mpsFeign.rebindDev(mpsId, matCode, stringVal(b.get("devNo")), stringVal(b.get("devName"))));
            result.getAffectedMpsIds().add(mpsId);
        }
    }

    private void previewChangeDev(List<MPSDto> allMps, Map<String, Object> payload, MoAdjustPreviewResult preview) {
        preview.getHints().add("will update r_mps_d.devNo for WAIT_* only");
        String mpsId = payload == null ? null : stringVal(payload.get("mpsId"));
        if (StringUtils.isNotBlank(mpsId)) {
            preview.getWouldAffectMpsIds().add(mpsId);
        }
    }

    private List<MPSVo> rebuildOnLine(MODto mo, String lineCode, int num) {
        LineVo line = feign(() -> lineFeign.selectByCode(lineCode));
        if (line == null) {
            throw new BusinessException("${mo.msg.line.no}");
        }
        List<TFVo> tfs = feign(() -> tfFeign.selectByLineId(line.getId()));
        List<TFEdgeVo> edges = feign(() -> tfEdgeFeign.selectByLineId(line.getId()));
        Map<String, List<String>> toPreCodes = new HashMap<>();
        if (CollectionUtil.isNotBlank(tfs) && CollectionUtil.isNotBlank(edges)) {
            Map<String, String> idToSelf = new HashMap<>();
            for (TFVo tf : tfs) {
                idToSelf.put(tf.getId(), tf.getSelfCode());
            }
            for (TFEdgeVo edge : edges) {
                if (edge == null) continue;
                String fromCode = idToSelf.get(edge.getFromTfId());
                if (fromCode == null || edge.getToTfId() == null) continue;
                toPreCodes.computeIfAbsent(edge.getToTfId(), k -> new ArrayList<>()).add(fromCode);
            }
        }
        List<MPSTFDto> mpstfDtos = new ArrayList<>();
        if (CollectionUtil.isNotBlank(tfs)) {
            for (TFVo tf : tfs) {
                MPSTFDto dto = new MPSTFDto();
                BeanUtils.copyProperties(tf, dto);
                List<String> pre = toPreCodes.get(tf.getId());
                if (CollectionUtil.isNotBlank(pre)) {
                    dto.setPre(String.join(",", pre));
                }
                mpstfDtos.add(dto);
            }
        }
        List<MPSFlowDetailDto> details = new ArrayList<>();
        if (CollectionUtil.isNotBlank(mo.getDetailDtoList())) {
            for (MODetailDto d : mo.getDetailDtoList()) {
                MPSFlowDetailDto fd = new MPSFlowDetailDto();
                fd.setId(d.getId());
                fd.setPriority(d.getPriority());
                fd.setDevNo(d.getDevNo());
                fd.setDevName(d.getDevName());
                fd.setDgCode(d.getDgCode());
                fd.setDgName(d.getDgName());
                details.add(fd);
            }
        }
        // reload mo with details if missing
        if (CollectionUtil.isBlank(mo.getDetailDtoList())) {
            mo = loadMo(mo.getSelfCode());
        }
        MPSFlowDto flow = new MPSFlowDto()
                .setMoCode(mo.getSelfCode())
                .setLine(lineCode)
                .setExecTime(mo.getExecTime())
                .setExecType(MoSplitEnum.part.getCode())
                .setExecNum(num)
                .setDetails(details)
                .setTfs(mpstfDtos);
        return mpsFlowManager.doFlow(flow);
    }

    // ---------------- reschedule / priority (P0) ----------------

    private void doReschedule(MODto mo, List<MPSDto> allMps, Map<String, Object> payload, MoAdjustResult result) {
        if (payload == null || payload.get("execTime") == null) {
            throw new BusinessException("${mo.adjust.execTime.required}");
        }
        Date execTime = parseDate(payload.get("execTime"));
        boolean dequeueQueued = payload.get("dequeueQueued") == null
                || Boolean.parseBoolean(String.valueOf(payload.get("dequeueQueued")));
        boolean syncMo = payload.get("syncMo") == null
                || Boolean.parseBoolean(String.valueOf(payload.get("syncMo")));
        List<String> targetIds = readStringList(payload, "mpsIds");
        List<MPSDto> targets;
        if (CollectionUtil.isBlank(targetIds)) {
            targets = allMps.stream().filter(m -> VOIDABLE_STATUSES.contains(m.getStatus())).collect(Collectors.toList());
        } else {
            Map<String, MPSDto> byId = indexById(allMps);
            targets = new ArrayList<>();
            for (String id : targetIds) {
                MPSDto mps = byId.get(id);
                if (mps == null) {
                    throw new BusinessException("${mo.adjust.mps.notfound}");
                }
                if (UNCANCELLABLE_STATUSES.contains(mps.getStatus())) {
                    throw new BusinessException("${mo.adjust.execing.reject}");
                }
                if (VOIDABLE_STATUSES.contains(mps.getStatus())) {
                    targets.add(mps);
                }
            }
        }
        for (MPSDto mps : targets) {
            if (dequeueQueued && MPSStatusEnum.WAIT_EXEC.getCode().equals(mps.getStatus())) {
                feign(() -> mpsFeign.removeQueue(new MPSQueueOperateDto().setId(mps.getId())));
            }
            result.getAffectedMpsIds().add(mps.getId());
        }
        if (CollectionUtil.isNotBlank(result.getAffectedMpsIds())) {
            feign(() -> mpsFeign.updateExecTime(result.getAffectedMpsIds(), execTime));
        }
        if (syncMo) {
            feign(() -> moFeign.updateExecTime(mo.getSelfCode(), execTime));
        }
    }

    private void doPriority(MODto mo, List<MPSDto> allMps, Map<String, Object> payload, MoAdjustResult result) {
        if (payload == null) {
            throw new BusinessException("${mo.adjust.payload.required}");
        }
        String mode = String.valueOf(payload.getOrDefault("mode", "JUMP")).trim().toUpperCase(Locale.ROOT);
        String mpsId = firstNonBlank(stringVal(payload.get("mpsId")), stringVal(payload.get("current")), stringVal(payload.get("id")));
        if (StringUtils.isBlank(mpsId)) {
            throw new BusinessException("${mo.adjust.mpsIds.required}");
        }
        MPSDto target = indexById(allMps).get(mpsId);
        if (target == null) {
            throw new BusinessException("${mo.adjust.mps.notfound}");
        }
        if (!mo.getSelfCode().equals(target.getMoCode())) {
            throw new BusinessException("${mo.adjust.mps.mo.mismatch}");
        }
        if (!MPSStatusEnum.WAIT_EXEC.getCode().equals(target.getStatus())) {
            throw new BusinessException("${mo.adjust.priority.status}");
        }
        if ("REORDER".equals(mode) || "CHANGE".equals(mode) || "CHANGE_PRIORITY".equals(mode)) {
            ChangePriorityDto dto = new ChangePriorityDto()
                    .setCurrent(mpsId)
                    .setPre(stringVal(payload.get("pre")))
                    .setPost(stringVal(payload.get("post")));
            feign(() -> mpsFeign.changePriority(dto));
        } else {
            feign(() -> mpsFeign.jumpQueue(new MPSQueueOperateDto().setId(mpsId)));
        }
        result.getAffectedMpsIds().add(mpsId);
    }

    // ---------------- shared helpers ----------------

    private void rollbackVoid(MODto mo, List<String> voidIds, boolean forceExecing, MoAdjustResult result) {
        if (CollectionUtil.isBlank(voidIds)) {
            return;
        }
        List<MPSDto> voided = feign(() -> mpsFeign.voidMpsCascade(voidIds, forceExecing));
        int rollback = 0;
        if (CollectionUtil.isNotBlank(voided)) {
            for (MPSDto mps : voided) {
                if (mps.getNum() != null) {
                    rollback += mps.getNum();
                }
                result.getVoidedMpsIds().add(mps.getId());
            }
        }
        if (rollback > 0) {
            int surplus = (mo.getSurplus() == null ? 0 : mo.getSurplus()) + rollback;
            feign(() -> moFeign.updateSurplus(mo.getSelfCode(), surplus));
            mo.setSurplus(surplus);
            result.setSurplusDelta((result.getSurplusDelta() == null ? 0 : result.getSurplusDelta()) + rollback);
        }
    }

    private void maybeAbortMes(String moCode, List<String> voidIds, boolean force, MoAdjustResult result) {
        if (!force || CollectionUtil.isBlank(voidIds)) {
            return;
        }
        try {
            Boolean started = RemoteHandleUtils.getDataFormResponse(
                    syncFeign.existSyncResource(SyncTxEnum.PLAN_START.name(), moCode));
            if (Boolean.TRUE.equals(started)) {
                result.getWarnings().add("PLAN_START exists; local CANCEL + abort stub");
                ygApi.abortPlan(moCode);
                try {
                    planAbortTxManager.execute(moCode);
                } catch (Exception e) {
                    log.warn("PLAN_ABORT tx start failed mo={}: {}", moCode, e.getMessage());
                    result.getWarnings().add("PLAN_ABORT skeleton failed: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("check PLAN_START failed mo={}: {}", moCode, e.getMessage());
            result.getWarnings().add("PLAN_START check failed: " + e.getMessage());
        }
    }

    private List<String> collectVoidIds(List<MPSDto> allMps, List<String> onlyIds, boolean force) {
        List<String> ids = new ArrayList<>();
        for (MPSDto mps : allMps) {
            if (onlyIds != null && !onlyIds.contains(mps.getId())) {
                continue;
            }
            if (VOIDABLE_STATUSES.contains(mps.getStatus())) {
                ids.add(mps.getId());
            } else if (force && MPSStatusEnum.EXECING.getCode().equals(mps.getStatus())) {
                ids.add(mps.getId());
            } else if (MPSStatusEnum.EXECING.getCode().equals(mps.getStatus())) {
                throw new BusinessException("${mo.adjust.execing.reject}");
            }
        }
        return ids;
    }

    private boolean hasExecing(List<MPSDto> allMps, List<String> onlyIds) {
        if (CollectionUtil.isBlank(allMps)) {
            return false;
        }
        for (MPSDto mps : allMps) {
            if (onlyIds != null && !onlyIds.contains(mps.getId())) {
                continue;
            }
            if (MPSStatusEnum.EXECING.getCode().equals(mps.getStatus())) {
                return true;
            }
        }
        return false;
    }

    void assertSurplus(MODto mo, List<MPSDto> mpsList) {
        if (mo == null || mo.getNum() == null) {
            throw new BusinessException("${mo.adjust.surplus.assert}");
        }
        int surplus = mo.getSurplus() == null ? 0 : mo.getSurplus();
        int activeSum = sumActive(mpsList);
        if (mo.getNum() != surplus + activeSum) {
            log.error("surplus assert failed mo={} num={} surplus={} activeSum={}",
                    mo.getSelfCode(), mo.getNum(), surplus, activeSum);
            throw new BusinessException("${mo.adjust.surplus.assert}");
        }
    }

    private int sumActive(List<MPSDto> mpsList) {
        int activeSum = 0;
        if (CollectionUtil.isNotBlank(mpsList)) {
            for (MPSDto mps : mpsList) {
                if (mps.getStatus() != null && ACTIVE_MPS_STATUSES.contains(mps.getStatus())) {
                    activeSum += mps.getNum() == null ? 0 : mps.getNum();
                }
            }
        }
        return activeSum;
    }

    private int sumByStatuses(List<MPSDto> mpsList, List<Integer> statuses) {
        int sum = 0;
        if (CollectionUtil.isBlank(mpsList)) {
            return 0;
        }
        for (MPSDto mps : mpsList) {
            if (mps.getStatus() != null && statuses.contains(mps.getStatus())) {
                sum += mps.getNum() == null ? 0 : mps.getNum();
            }
        }
        return sum;
    }

    private int sumNums(List<MPSDto> mpsList, List<String> ids) {
        if (CollectionUtil.isBlank(ids)) {
            return 0;
        }
        Set<String> set = new HashSet<>(ids);
        int sum = 0;
        for (MPSDto mps : mpsList) {
            if (set.contains(mps.getId())) {
                sum += mps.getNum() == null ? 0 : mps.getNum();
            }
        }
        return sum;
    }

    private void requireForceOperator(MoAdjustCommand command) {
        if (StringUtils.isBlank(command.getOperator())) {
            throw new BusinessException("${mo.adjust.force.operator}");
        }
    }

    private void validateCommand(MoAdjustCommand command) {
        if (command == null) {
            throw new BusinessException("${mo.adjust.payload.required}");
        }
        if (StringUtils.isBlank(command.getMoCode())) {
            throw new BusinessException("${mo.adjust.moCode.required}");
        }
        if (StringUtils.isBlank(command.getAdjustType())) {
            throw new BusinessException("${mo.adjust.type.invalid}");
        }
        if (StringUtils.isBlank(command.getRequestId())) {
            throw new BusinessException("${mo.adjust.requestId.required}");
        }
    }

    private MODto loadMo(String moCode) {
        MODto mo = feign(() -> moFeign.selectByCode(moCode));
        if (mo == null) {
            throw new BusinessException("${mo.msg.exist.no}");
        }
        return mo;
    }

    private List<MPSDto> loadMps(String moCode) {
        List<MPSDto> list = feign(() -> mpsFeign.selectByMoCode(moCode));
        return list == null ? new ArrayList<>() : list;
    }

    private Map<String, MPSDto> indexById(List<MPSDto> allMps) {
        return allMps.stream()
                .filter(m -> m.getId() != null)
                .collect(Collectors.toMap(MPSDto::getId, m -> m, (a, b) -> a));
    }

    private MoAdjustResult replayResult(MoAdjustLogDto existing) {
        MoAdjustResult result = new MoAdjustResult()
                .setLogId(existing.getId())
                .setRequestId(existing.getRequestId())
                .setMoCode(existing.getMoCode())
                .setAdjustType(existing.getAdjustType())
                .setIdempotentReplay(true)
                .setMo(loadMo(existing.getMoCode()));
        if (StringUtils.isNotBlank(existing.getAffectMpsIds())) {
            result.setAffectedMpsIds(new ArrayList<>(Arrays.asList(existing.getAffectMpsIds().split(","))));
            result.setVoidedMpsIds(new ArrayList<>(result.getAffectedMpsIds()));
        }
        return result;
    }

    private Map<String, Object> snapshot(MODto mo, List<MPSDto> mpsList) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("mo", mo);
        map.put("mps", mpsList);
        return map;
    }

    private List<String> readStringList(Map<String, Object> payload, String key) {
        if (payload == null || payload.get(key) == null) {
            return Collections.emptyList();
        }
        Object raw = payload.get(key);
        if (raw instanceof Collection<?> col) {
            List<String> r = new ArrayList<>();
            for (Object o : col) {
                if (o != null && StringUtils.isNotBlank(String.valueOf(o))) {
                    r.add(String.valueOf(o));
                }
            }
            return r;
        }
        String s = String.valueOf(raw);
        if (StringUtils.isBlank(s)) {
            return Collections.emptyList();
        }
        return Arrays.stream(s.split(",")).map(String::trim).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readMapList(Map<String, Object> payload, String key) {
        if (payload == null || payload.get(key) == null) {
            return Collections.emptyList();
        }
        Object raw = payload.get(key);
        if (raw instanceof List<?> list) {
            List<Map<String, Object>> r = new ArrayList<>();
            for (Object o : list) {
                if (o instanceof Map<?, ?> m) {
                    r.add((Map<String, Object>) m);
                }
            }
            return r;
        }
        return Collections.emptyList();
    }

    private int readPositiveInt(Map<String, Object> payload, String key, String label) {
        Integer v = readInt(payload, key);
        if (v == null || v <= 0) {
            throw new BusinessException("${mo.adjust.qty.required}");
        }
        return v;
    }

    private Integer readInt(Map<String, Object> payload, String key) {
        if (payload == null || payload.get(key) == null) {
            return null;
        }
        Object raw = payload.get(key);
        if (raw instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(raw).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Date parseDate(Object raw) {
        if (raw instanceof Date date) {
            return date;
        }
        if (raw instanceof Number number) {
            return new Date(number.longValue());
        }
        String text = String.valueOf(raw).trim();
        if (StringUtils.isBlank(text)) {
            throw new BusinessException("${mo.adjust.execTime.required}");
        }
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };
        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern).parse(text);
            } catch (ParseException ignore) {
            }
        }
        try {
            return new Date(Long.parseLong(text));
        } catch (NumberFormatException e) {
            throw new BusinessException("${mo.adjust.execTime.invalid}");
        }
    }

    private String stringVal(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.isNotBlank(v) && !"null".equalsIgnoreCase(v)) {
                return v;
            }
        }
        return null;
    }

    @FunctionalInterface
    private interface FeignCall<T> {
        com.ourexists.era.framework.core.model.vo.JsonResponseEntity<T> call() throws EraCommonException;
    }

    private <T> T feign(FeignCall<T> call) {
        try {
            return RemoteHandleUtils.getDataFormResponse(call.call());
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
    }
}
