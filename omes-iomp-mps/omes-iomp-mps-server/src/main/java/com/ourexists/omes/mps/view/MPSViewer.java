/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mps.view;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.omes.mps.enums.MPSStatusEnum;
import com.ourexists.omes.mps.enums.MPSTFStatusEnum;
import com.ourexists.omes.mps.feign.MPSFeign;
import com.ourexists.omes.mps.model.ChangePriorityDto;
import com.ourexists.omes.mps.model.MPSBoardDto;
import com.ourexists.omes.mps.model.MPSDetailDto;
import com.ourexists.omes.mps.model.MPSDto;
import com.ourexists.omes.mps.model.MPSQueueOperateDto;
import com.ourexists.omes.mps.model.query.MPSBoardQuery;
import com.ourexists.omes.mps.model.query.MPSPageQuery;
import com.ourexists.omes.mps.pojo.MPS;
import com.ourexists.omes.mps.pojo.MPSDetail;
import com.ourexists.omes.mps.pojo.MPSTF;
import com.ourexists.omes.mps.service.MPSDetailService;
import com.ourexists.omes.mps.service.MPSService;
import com.ourexists.omes.mps.service.MPSTFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Tag(name = "生产计划")
//@RestController
//@RequestMapping("/mps")
@Component
public class MPSViewer implements MPSFeign {

    @Autowired
    private MPSService service;

    @Autowired
    private MPSDetailService mpsDetailService;

    @Autowired
    private MPSTFService mpsTFService;

    //    @Operation(summary = "分页查询", description = "分页查询")
//    @PostMapping("selectByPage")
    public JsonResponseEntity<List<MPSDto>> selectByPage(@RequestBody MPSPageQuery dto) {
        Page<MPS> page = service.selectByPage(dto);
        List<MPSDto> r = MPS.covert(page.getRecords());
        if (CollectionUtil.isBlank(r)) {
            return JsonResponseEntity.success(r, OrmUtils.extraPagination(page));
        }

        List<MPSDetail> details = null;
        if (dto.getQueryDetail()) {
            List<String> ids = r.stream().map(MPSDto::getId).collect(Collectors.toList());
            details = mpsDetailService.selectByMid(ids);
        }
        for (MPSDto mpsDto : r) {
            if (CollectionUtil.isNotBlank(details)) {
                List<MPSDetailDto> dtos = new ArrayList<>();
                for (MPSDetail detail : details) {
                    if (mpsDto.getId().equals(detail.getMid())) {
                        dtos.add(MPSDetail.covert(detail));
                    }
                }
                mpsDto.setDetails(dtos);
            }
        }
        return JsonResponseEntity.success(r, OrmUtils.extraPagination(page));
    }

    @Override
    public JsonResponseEntity<MPSBoardDto> selectBoard(MPSBoardQuery query) {
        int limit = query.getLimitPerColumn() == null || query.getLimitPerColumn() <= 0
                ? 300
                : query.getLimitPerColumn();
        List<MPS> all = service.selectBoardList(query);
        if (CollectionUtil.isBlank(all)) {
            return JsonResponseEntity.success(new MPSBoardDto());
        }

        Comparator<MPS> idDesc = Comparator.comparing(MPS::getId, Comparator.nullsLast(Comparator.reverseOrder()));
        Comparator<MPS> priorityAsc = Comparator.comparing(
                MPS::getPriority,
                Comparator.nullsLast(Comparator.naturalOrder())
        ).thenComparing(MPS::getId, Comparator.nullsLast(Comparator.reverseOrder()));

        Map<Integer, List<MPS>> grouped = all.stream().collect(Collectors.groupingBy(MPS::getStatus));
        MPSBoardDto board = new MPSBoardDto();
        board.setWaitQue(limitList(grouped.get(MPSStatusEnum.WAIT_QUE.getCode()), idDesc, limit));
        board.setWaitExec(limitList(grouped.get(MPSStatusEnum.WAIT_EXEC.getCode()), priorityAsc, limit));
        board.setExecing(limitList(grouped.get(MPSStatusEnum.EXECING.getCode()), idDesc, limit));
        board.setComplete(limitList(grouped.get(MPSStatusEnum.COMPLETE.getCode()), idDesc, limit));
        return JsonResponseEntity.success(board);
    }

    private List<MPSDto> limitList(List<MPS> source, Comparator<MPS> comparator, int limit) {
        if (CollectionUtil.isBlank(source)) {
            return new ArrayList<>();
        }
        return MPS.covert(source.stream().sorted(comparator).limit(limit).collect(Collectors.toList()));
    }

    //    @Operation(summary = "id查詢", description = "id查詢")
//    @GetMapping("selectById")
    public JsonResponseEntity<MPSDto> selectById(@RequestParam String id) {
        MPSDto mps = MPS.covert(this.service.getById(id));
        if (mps != null) {
            mps.setDetails(MPSDetail.covert(mpsDetailService.selectByMid(id)));
            mps.setTfs(MPSTF.covert(mpsTFService.selectByMPSId(id)));
        }
        return JsonResponseEntity.success(mps);
    }

    @Override
    public JsonResponseEntity<MPSDto> selectByCode(String code) {
        MPS mps = this.service.getOne(new LambdaQueryWrapper<MPS>().eq(MPS::getMoCode, code));
        if (mps == null) {
            return new JsonResponseEntity<>(null);
        }
        List<MPSTF> mpstfs = mpsTFService.list(new LambdaUpdateWrapper<MPSTF>()
                .eq(MPSTF::getMpsId, mps.getId())
        );
        MPSDto mpsDto = MPS.covert(mps);
        mpsDto.setTfs(MPSTF.covert(mpstfs));
        return JsonResponseEntity.success(mpsDto);
    }

    //    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
//    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody MPSDto dto) {
        service.saveOrUpdate(MPS.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    //    @Operation(summary = "批量新增", description = "批量新增")
//    @PostMapping("addBatch")
    public JsonResponseEntity<Boolean> addBatch(@Validated @RequestBody List<MPSDto> dtos) {
        service.addBatch(dtos);
        return JsonResponseEntity.success(true);
    }

    //    @Operation(summary = "删除", description = "已废弃：取消请走 /mo/adjust")
//    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        throw new com.ourexists.era.framework.core.exceptions.BusinessException("${mps.msg.delete.use.adjust}");
    }

    //    @Operation(summary = "改变优先级", description = "改变优先级")
//    @PostMapping("changePriority")
    public JsonResponseEntity<Boolean> changePriority(@Validated @RequestBody ChangePriorityDto dto) {
        service.changePriority(dto);
        return JsonResponseEntity.success(true);
    }

    //    @Operation(summary = "批量加入生产队列", description = "批量加入生产队列")
//    @PostMapping("joinQueueBatch")
    public JsonResponseEntity<Boolean> joinQueueBatch(@Validated @RequestBody List<String> ids) {
        service.joinQueueBatch(ids);
        return JsonResponseEntity.success(true);
    }

    //    @Operation(summary = "加入生产队列", description = "加入生产队列")
//    @PostMapping("joinQueue")
    public JsonResponseEntity<Boolean> joinQueue(@Validated @RequestBody MPSQueueOperateDto dto) {
        service.joinQueue(dto);
        return JsonResponseEntity.success(true);
    }

    //    @Operation(summary = "插队", description = "插队")
//    @PostMapping("jumpQueue")
    public JsonResponseEntity<Boolean> jumpQueue(@Validated @RequestBody MPSQueueOperateDto dto) {
        service.jumpQueue(dto);
        return JsonResponseEntity.success(true);
    }

    //    @Operation(summary = "移出生产队列", description = "移出生产队列")
//    @PostMapping("removeQueue")
    public JsonResponseEntity<Boolean> removeQueue(@Validated @RequestBody MPSQueueOperateDto dto) {
        service.removeQueue(dto);
        return JsonResponseEntity.success(true);
    }

    //    @Operation(summary = "流程开始", description = "流程开始")
//    @GetMapping("startTf")
    public JsonResponseEntity<Boolean> startTf(@RequestParam String tfId) {
        mpsTFService.updateStatus(tfId, MPSTFStatusEnum.EXEC);
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<Long> countExecByCode(@RequestParam String lineCode) {
        Long count = service.count(new LambdaQueryWrapper<MPS>()
                .in(MPS::getStatus, Arrays.asList(MPSStatusEnum.WAIT_EXEC.getCode(), MPSStatusEnum.WAIT_QUE.getCode(), MPSStatusEnum.EXECING.getCode()))
                .eq(MPS::getLine, lineCode));
        return JsonResponseEntity.success(count);
    }

    @Override
    public JsonResponseEntity<Integer> getMaxBatch(@RequestParam String moCode) {
        return JsonResponseEntity.success(service.getMaxBatch(moCode));
    }

    @Override
    public JsonResponseEntity<List<MPSDto>> selectByStatus(@RequestParam MPSStatusEnum mpsStatusEnum) {
        List<MPS> mpsList = service.selectByStatus(MPSStatusEnum.COMPLETE);
        List<MPSDto> mpsDtos = new ArrayList<>();
        for (MPS mps : mpsList) {
            MPSDto mpsDto = MPS.covert(mps);
            List<MPSTF> mpstfs = mpsTFService.list(new LambdaUpdateWrapper<MPSTF>()
                    .eq(MPSTF::getMpsId, mps.getId())
            );
            mpsDto.setTfs(MPSTF.covert(mpstfs));
        }
        return JsonResponseEntity.success(mpsDtos);
    }

    @Override
    public JsonResponseEntity<List<MPSDto>> selectEnabledJoinQueMps() {
        return JsonResponseEntity.success(MPS.covert(service.selectEnabledJoinQueMps()));
    }

    @Override
    public JsonResponseEntity<Boolean> adjustToJoinQue() {
        service.adjustToJoinQue();
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<Boolean> joinQueueBatchByMoCodesLimitEnable(@RequestBody List<String> moCodes) {
        service.joinQueueBatchByMoCodesLimitEnable(moCodes);
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<List<MPSDto>> selectByMoCode(@RequestParam String moCode) {
        return JsonResponseEntity.success(MPS.covert(service.selectByMoCode(moCode)));
    }

    @Override
    public JsonResponseEntity<List<MPSDto>> voidMpsCascade(@RequestBody List<String> ids,
                                                           @RequestParam(required = false, defaultValue = "false") Boolean forceExecing) {
        return JsonResponseEntity.success(MPS.covert(service.voidMpsCascade(ids, Boolean.TRUE.equals(forceExecing))));
    }

    @Override
    public JsonResponseEntity<Boolean> updateExecTime(@RequestBody List<String> ids,
                                                      @RequestParam java.util.Date execTime) {
        service.updateExecTime(ids, execTime);
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<Boolean> rebindDev(@RequestParam String mpsId,
                                                 @RequestParam String matCode,
                                                 @RequestParam(required = false) String devNo,
                                                 @RequestParam(required = false) String devName) {
        service.rebindDev(mpsId, matCode, devNo, devName);
        return JsonResponseEntity.success(true);
    }
}
