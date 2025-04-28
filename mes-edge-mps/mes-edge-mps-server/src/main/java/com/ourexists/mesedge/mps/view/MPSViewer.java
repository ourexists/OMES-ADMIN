/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.view;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.mps.enums.MPSStatusEnum;
import com.ourexists.mesedge.mps.enums.MPSTFStatusEnum;
import com.ourexists.mesedge.mps.feign.MPSFeign;
import com.ourexists.mesedge.mps.model.ChangePriorityDto;
import com.ourexists.mesedge.mps.model.MPSDetailDto;
import com.ourexists.mesedge.mps.model.MPSDto;
import com.ourexists.mesedge.mps.model.MPSQueueOperateDto;
import com.ourexists.mesedge.mps.model.query.MPSPageQuery;
import com.ourexists.mesedge.mps.pojo.MPS;
import com.ourexists.mesedge.mps.pojo.MPSDetail;
import com.ourexists.mesedge.mps.pojo.MPSTF;
import com.ourexists.mesedge.mps.service.MPSDetailService;
import com.ourexists.mesedge.mps.service.MPSService;
import com.ourexists.mesedge.mps.service.MPSTFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
                    if (mpsDto.getId().equals(detail.getId())) {
                        dtos.add(MPSDetail.covert(detail));
                    }
                }
                mpsDto.setDetails(dtos);
            }
        }
        return JsonResponseEntity.success(r, OrmUtils.extraPagination(page));
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

    //    @Operation(summary = "删除", description = "删除")
//    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
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
}
