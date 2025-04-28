/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.DateUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.mps.enums.MPSStatusEnum;
import com.ourexists.mesedge.mps.mapper.MPSMapper;
import com.ourexists.mesedge.mps.model.ChangePriorityDto;
import com.ourexists.mesedge.mps.model.MPSDto;
import com.ourexists.mesedge.mps.model.MPSQueueOperateDto;
import com.ourexists.mesedge.mps.model.query.MPSPageQuery;
import com.ourexists.mesedge.mps.pojo.MPS;
import com.ourexists.mesedge.mps.pojo.MPSDetail;
import com.ourexists.mesedge.mps.pojo.MPSTF;
import com.ourexists.mesedge.mps.service.MPSDetailService;
import com.ourexists.mesedge.mps.service.MPSService;
import com.ourexists.mesedge.mps.service.MPSTFService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MPSServiceImpl extends AbstractMyBatisPlusService<MPSMapper, MPS> implements MPSService {


    @Autowired
    private MPSDetailService mpsDetailService;

    @Autowired
    private MPSTFService mpstfService;

    @Override
    public Page<MPS> selectByPage(MPSPageQuery dto) {
        LambdaQueryWrapper<MPS> qw = new LambdaQueryWrapper<MPS>()
                .eq(dto.getStatus() != null, MPS::getStatus, dto.getStatus())
                .eq(StringUtils.isNotEmpty(dto.getMoCode()), MPS::getMoCode, dto.getMoCode())
                .in(CollectionUtil.isNotBlank(dto.getMoCodes()), MPS::getMoCode, dto.getMoCodes())
                .ge(dto.getExecStartTime() != null, MPS::getExecTime, dto.getExecStartTime())
                .le(dto.getExecEndTime() != null, MPS::getExecTime, dto.getExecEndTime())
                .orderByAsc(dto.getPrioritySort(), MPS::getPriority)
                .orderByDesc(MPS::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T extends MPSDto> void addBatch(List<T> dtos) {
        List<MPS> mps = new ArrayList<>();
        List<MPSDetail> mpsDetails = new ArrayList<>();
        List<MPSTF> mpsTfs = new ArrayList<>();
        for (MPSDto dto : dtos) {
            MPS mps1 = MPS.wrap(dto);
            mps1.setId(IdWorker.getIdStr());
            mps.add(mps1);
            if (CollectionUtil.isNotBlank(dto.getDetails())) {
                mpsDetails.addAll(MPSDetail.wrap(dto.getDetails(), mps1.getId(), dto.getMoCode()));
            }
            if (CollectionUtil.isNotBlank(dto.getTfs())) {
                mpsTfs.addAll(MPSTF.wrap(dto.getTfs(), mps1.getId(), dto.getMoCode(), true));
            }
        }
        if (CollectionUtil.isNotBlank(mps)) {
            if (this.saveBatch(mps)) {
                if (!CollectionUtil.isBlank(mpsDetails)) {
                    mpsDetailService.saveBatch(mpsDetails);
                }
                if (!CollectionUtil.isBlank(mpsTfs)) {
                    mpstfService.saveBatch(mpsTfs);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePriority(ChangePriorityDto dto) {
        List<String> allIds = new ArrayList<>();

        if (StringUtils.isNotBlank(dto.getPre())) {
            allIds.add(dto.getPre());
        }
        if (StringUtils.isNotBlank(dto.getPost())) {
            allIds.add(dto.getPost());
        }
        List<MPS> mps = this.listByIds(allIds);
        BigDecimal newPriority = BigDecimal.ZERO;
        for (MPS mp : mps) {
            newPriority = newPriority.add(mp.getPriority());
        }
        if (StringUtils.isBlank(dto.getPost())) {
            newPriority = newPriority.add(BigDecimal.ONE);
        } else {
            newPriority = newPriority.divide(BigDecimal.valueOf(2), 5, RoundingMode.HALF_UP);
        }
        this.update(new LambdaUpdateWrapper<MPS>().set(MPS::getPriority, newPriority).eq(MPS::getId, dto.getCurrent()));
    }

    @Override
    public void jumpQueue(MPSQueueOperateDto dto) {
        BigDecimal minPriority = this.baseMapper.getMinPriority(MPSStatusEnum.WAIT_EXEC.getCode());
        if (minPriority == null) {
            minPriority = BigDecimal.ONE;
        } else {
            minPriority = minPriority.subtract(BigDecimal.valueOf(0.1));
        }
        this.update(
                new LambdaUpdateWrapper<MPS>()
                        .set(MPS::getPriority, minPriority)
                        .eq(MPS::getId, dto.getId())
                        .eq(MPS::getStatus, MPSStatusEnum.WAIT_EXEC.getCode())
        );
    }

    @Override
    public List<MPS> selectByMoCode(String moCode) {
        return this.list(new LambdaQueryWrapper<MPS>()
                .eq(MPS::getMoCode, moCode)
                .orderByAsc(MPS::getPriority)
                .orderByDesc(MPS::getId)
        );
    }

    @Override
    public List<MPS> selectByStatus(MPSStatusEnum status) {
        return this.list(new LambdaQueryWrapper<MPS>()
                .eq(MPS::getStatus, status.getCode())
                .orderByAsc(MPS::getPriority)
                .orderByDesc(MPS::getId)
        );
    }

    @Override
    public List<MPS> selectByIds(Collection<String> ids) {
        return this.list(
                new LambdaQueryWrapper<MPS>()
                        .in(MPS::getId, ids)
                        .orderByAsc(MPS::getPriority)
                        .orderByDesc(MPS::getId)
        );
    }

    @Override
    public List<MPS> selectByIdsAndStatus(Collection<String> ids, MPSStatusEnum status) {
        return this.list(
                new LambdaQueryWrapper<MPS>()
                        .eq(MPS::getStatus, status.getCode())
                        .in(MPS::getId, ids)
                        .orderByAsc(MPS::getPriority)
                        .orderByDesc(MPS::getId)
        );
    }

    @Override
    public int getMaxBatch(String moCode) {
        List<MPS> mpsList = this.selectByMoCode(moCode);
        int max = -1;
        if (CollectionUtil.isNotBlank(mpsList)) {
            for (MPS mps : mpsList) {
                if (mps.getBatch() != null && mps.getBatch() > max) {
                    max = mps.getBatch();
                }
            }
        }
        return max;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(String id, MPSStatusEnum mpsStatus) {
        return this.update(new LambdaUpdateWrapper<MPS>()
                .set(MPS::getStatus, mpsStatus.getCode())
                .eq(MPS::getId, id)
                .eq(mpsStatus.getPreCode() != null, MPS::getStatus, mpsStatus.getPreCode()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustToJoinQue() {
        List<MPS> mpsList = this.selectEnabledJoinQueMps();
        if (CollectionUtil.isBlank(mpsList)) {
            return;
        }
        List<String> ids = mpsList.stream().map(MPS::getId).collect(Collectors.toList());
        this.joinQueueBatch(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinQueueBatch(List<String> ids) {
        BigDecimal maxPriority = this.baseMapper.getMaxPriority(MPSStatusEnum.WAIT_EXEC.getCode());
        if (maxPriority == null) {
            maxPriority = BigDecimal.ZERO;
        }
        this.update(
                new LambdaUpdateWrapper<MPS>()
                        .set(MPS::getStatus, MPSStatusEnum.WAIT_EXEC.getCode())
                        .setSql("priority=(sequence*1000+batch*10+" + maxPriority + ")")
                        .in(MPS::getId, ids)
                        .eq(MPS::getStatus, MPSStatusEnum.WAIT_EXEC.getPreCode())
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinQueue(MPSQueueOperateDto dto) {
        BigDecimal maxPriority = this.baseMapper.getMaxPriority(MPSStatusEnum.WAIT_EXEC.getCode());
        if (maxPriority == null) {
            maxPriority = BigDecimal.ONE;
        } else {
            maxPriority = maxPriority.add(BigDecimal.ONE);
        }
        this.update(
                new LambdaUpdateWrapper<MPS>()
                        .set(MPS::getStatus, MPSStatusEnum.WAIT_EXEC.getCode())
                        .set(MPS::getPriority, maxPriority)
                        .eq(MPS::getId, dto.getId())
                        .eq(MPS::getStatus, MPSStatusEnum.WAIT_EXEC.getPreCode())
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinQueueBatchByMoCode(String moCode) {
        BigDecimal maxPriority = getMaxPriority();
        this.update(
                new LambdaUpdateWrapper<MPS>()
                        .set(MPS::getStatus, MPSStatusEnum.WAIT_EXEC.getCode())
                        .setSql("priority=(sequence*1000+batch*10+" + maxPriority + ")")
                        .eq(MPS::getMoCode, moCode)
                        .eq(MPS::getStatus, MPSStatusEnum.WAIT_EXEC.getPreCode())
        );
    }

    @Override
    public List<MPS> selectEnabledJoinQueMps() {
        Date now = new Date();
        return this.list(
                new LambdaQueryWrapper<MPS>()
                        .eq(MPS::getStatus, MPSStatusEnum.WAIT_QUE.getCode())
                        .le(MPS::getExecTime, now)
                        .orderByAsc(MPS::getPriority)
                        .orderByDesc(MPS::getId)
        );
    }

    @Override
    public void joinQueueBatchByMoCodes(List<String> moCodes) {
        BigDecimal maxPriority = getMaxPriority();
        this.update(
                new LambdaUpdateWrapper<MPS>()
                        .set(MPS::getStatus, MPSStatusEnum.WAIT_EXEC.getCode())
                        .setSql("priority=(sequence*1000+batch*10+" + maxPriority + ")")
                        .in(MPS::getMoCode, moCodes)
                        .eq(MPS::getStatus, MPSStatusEnum.WAIT_EXEC.getPreCode())
        );
    }

    @Override
    public void joinQueueBatchByMoCodesLimitEnable(List<String> moCodes) {
        BigDecimal maxPriority = getMaxPriority();
        this.update(
                new LambdaUpdateWrapper<MPS>()
                        .set(MPS::getStatus, MPSStatusEnum.WAIT_EXEC.getCode())
                        .setSql("priority=(sequence*1000+batch*10+" + maxPriority + ")")
                        .in(MPS::getMoCode, moCodes)
                        .eq(MPS::getStatus, MPSStatusEnum.WAIT_EXEC.getPreCode())
                        .le(MPS::getExecTime, DateUtil.getCurrentSystemTime())
        );
    }

    private BigDecimal getMaxPriority() {
        BigDecimal maxPriority = this.baseMapper.getMaxPriority(MPSStatusEnum.WAIT_EXEC.getCode());
        if (maxPriority == null) {
            maxPriority = BigDecimal.ZERO;
        }
        return maxPriority;
    }


    @Override
    public void removeQueue(MPSQueueOperateDto dto) {
        this.update(
                new LambdaUpdateWrapper<MPS>()
                        .set(MPS::getStatus, MPSStatusEnum.WAIT_QUE.getCode())
                        .set(MPS::getPriority, BigDecimal.ZERO)
                        .eq(MPS::getId, dto.getId())
                        .eq(MPS::getStatus, MPSStatusEnum.WAIT_EXEC.getCode())
        );
    }


}
