/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.mps.enums.MPSStatusEnum;
import com.ourexists.mesedge.mps.enums.MPSTFStatusEnum;
import com.ourexists.mesedge.mps.mapper.MPSTFMapper;
import com.ourexists.mesedge.mps.pojo.MPS;
import com.ourexists.mesedge.mps.pojo.MPSTF;
import com.ourexists.mesedge.mps.service.MPSService;
import com.ourexists.mesedge.mps.service.MPSTFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class MPSTFServiceImpl extends AbstractMyBatisPlusService<MPSTFMapper, MPSTF> implements MPSTFService {

    @Autowired
    private MPSService mpsService;

//    @Autowired
//    private QAService qaService;

    @Override
    public MPSTF selectById(String id) {
        return getById(id);
    }

    @Override
    public List<MPSTF> selectByIds(List<String> ids) {
        if (CollectionUtil.isBlank(ids)) {
            return Collections.emptyList();
        }
        return this.list(
                new LambdaQueryWrapper<MPSTF>()
                        .in(MPSTF::getId, ids)
                        .orderByAsc(MPSTF::getId));
    }

    @Override
    public List<MPSTF> selectByMPSId(String mpsId) {
        return list(new LambdaUpdateWrapper<MPSTF>().eq(MPSTF::getMpsId, mpsId).orderByAsc(MPSTF::getId));
    }

    @Override
    public List<MPSTF> selectByMPSId(List<String> mids) {
        if (CollectionUtil.isBlank(mids)) {
            return Collections.emptyList();
        }
        return this.list(
                new LambdaQueryWrapper<MPSTF>()
                        .in(MPSTF::getMpsId, mids)
                        .orderByAsc(MPSTF::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String mpstfId, MPSTFStatusEnum mpstfStatus) {
        MPSTF mpstf = this.selectById(mpstfId);
        if (mpstf == null) {
            return;
        }
        //获取所有的流程
        List<MPSTF> tfs = this.selectByMPSId(mpstf.getMpsId());
        MPSStatusEnum mpsStatusEnum = null;

        //判断当前流程位置
        int index = -1;
        for (int i = 0; i < tfs.size(); i++) {
            if (tfs.get(i).getId().equals(mpstf.getId())) {
                index = i;
                break;
            }
        }


        if (mpstfStatus.equals(MPSTFStatusEnum.EXEC)) {
            //如果是第一条.判断计划状态
            if (index == 0) {
                MPS mps = mpsService.getById(mpstf.getMpsId());
                if (mps == null) {
                    throw new BusinessException("${common.msg.data.error}");
                }
                if (!mps.getStatus().equals(MPSStatusEnum.WAIT_EXEC.getCode())
                        && !mps.getStatus().equals(MPSStatusEnum.EXECING.getCode())) {
                    throw new BusinessException("${mps.msg.status.nomatch}");
                }
                if (mps.getStatus().equals(MPSStatusEnum.WAIT_EXEC.getCode())) {
                    mpsStatusEnum = MPSStatusEnum.EXECING;
                }
            } else {
                MPSTF pre = tfs.get(index - 1);
                if (!MPSTFStatusEnum.COMPLETE.getCode().equals(pre.getStatus())) {
                    throw new BusinessException("${mpstf.msg.pre.nocomplete}");
                }
            }
//            if (TFTypeEnum.QA.getCode().equals(mpstf.getType())) {
//                QA qa = new QA()
//                        .setMpsId(mpstf.getMpsId())
//                        .setMpsTfId(mpstf.getId())
//                        .setResult(QAResultEnum.NOT.getCode());
//                //目前单流程只支持一条
//                qaService.remove(new LambdaUpdateWrapper<QA>().eq(QA::getMpsTfId, mpstf.getId()));
//                if (!qaService.save(qa)) {
//                    throw new BusinessException("${common.msg.error}");
//                }
//            }
        } else if (mpstfStatus.equals(MPSTFStatusEnum.COMPLETE)) {
            //最后一条
            if (tfs.size() == index + 1) {
                mpsStatusEnum = MPSStatusEnum.COMPLETE;
            }
        }
        boolean r = this.update(new LambdaUpdateWrapper<MPSTF>()
                .set(MPSTF::getStatus, mpstfStatus.getCode())
                .set(mpstfStatus.equals(MPSTFStatusEnum.EXEC), MPSTF::getStartTime, new Date())
                .set(mpstfStatus.equals(MPSTFStatusEnum.COMPLETE), MPSTF::getEndTime, new Date())
                .eq(MPSTF::getId, mpstfId)
                .eq(mpstfStatus.getPreCode() != null && !mpstfStatus.equals(MPSTFStatusEnum.COMPLETE), MPSTF::getStatus, mpstfStatus.getPreCode()));
        if (!r) {
            throw new BusinessException("${mps.msg.status.nomatch}");
        }
        if (mpsStatusEnum != null) {
            boolean r2 = mpsService.updateStatus(mpstf.getMpsId(), mpsStatusEnum);
            if (!r2) {
                throw new BusinessException("${mps.msg.status.nomatch}");
            }
        }
    }
}
