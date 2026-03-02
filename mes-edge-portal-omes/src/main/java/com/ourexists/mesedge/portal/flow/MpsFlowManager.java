/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.flow;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.line.feign.LineFeign;
import com.ourexists.mesedge.line.model.LineVo;
import com.ourexists.mesedge.mo.enums.MOStatusEnum;
import com.ourexists.mesedge.mo.enums.MoSplitEnum;
import com.ourexists.mesedge.mo.feign.MOFeign;
import com.ourexists.mesedge.mo.model.MODetailDto;
import com.ourexists.mesedge.mo.model.MODto;
import com.ourexists.mesedge.mps.feign.MPSFeign;
import com.ourexists.mesedge.mps.model.*;
import com.ourexists.mesedge.portal.mps.model.MPSVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MpsFlowManager {

    @Autowired
    private MOFeign moFeign;

    @Autowired
    private MPSFeign mpsFeign;

    @Autowired
    private LineFeign lineFeign;

    public List<MPSVo> flowCalc(MPSFlowDto flowDto) {
        MODto mo;
        try {
            mo = RemoteHandleUtils.getDataFormResponse(moFeign.selectByCode(flowDto.getMoCode()));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        if (mo == null) {
            throw new BusinessException("${mo.msg.exist.no}");
        }
        if (!mo.getStatus().equals(MOStatusEnum.INIT.getCode()) && !mo.getStatus().equals(MOStatusEnum.PART.getCode())) {
            throw new BusinessException("${mo.msg.status.error}");
        }
        LineVo line;
        try {
            line = RemoteHandleUtils.getDataFormResponse(lineFeign.selectByCode(flowDto.getLine()));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        List<MPSTFDto> mpstfDtos = flowDto.getTfs();
        if (CollectionUtil.isNotBlank(mpstfDtos)) {
            mpstfDtos.forEach(mpstfDto -> {
                mpstfDto.setId(null);
            });
        }
        if (flowDto.getExecNum() == null) {
            flowDto.setExecNum(mo.getNum());
        }
        List<MPSVo> r = new ArrayList<>();
        MoSplitEnum moSplitEnum = MoSplitEnum.valueOf(flowDto.getExecType());

        Integer maxBatch = 0;
        try {
            maxBatch = RemoteHandleUtils.getDataFormResponse(mpsFeign.getMaxBatch(mo.getSelfCode()));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        int batch = maxBatch + 1;
        switch (moSplitEnum) {
            case disposable:
                Integer totalNums = mo.getSurplus();
                while (totalNums > 0) {
                    int num;
                    if (totalNums > flowDto.getExecNum()) {
                        num = flowDto.getExecNum();
                        totalNums -= flowDto.getExecNum();
                    } else {
                        num = totalNums;
                        totalNums = 0;
                    }
                    r.add(generateMps(flowDto, mo, line, mo.getDetailDtoList(), batch, num));
                    batch++;
                }
                break;
            case part:
                r.add(generateMps(flowDto, mo, line, mo.getDetailDtoList(), batch, flowDto.getExecNum()));
                break;
        }
        return r;
    }


    private MPSVo generateMps(MPSFlowDto flowDto, MODto mo, LineVo line, List<MODetailDto> moDetails, int batch, int num) {
        MPSVo dto = new MPSVo();
        BeanUtils.copyProperties(flowDto, dto);
        dto.setBatch(batch);
        dto.setNum(num);
        dto.setWeight(mo.getWeight().multiply(BigDecimal.valueOf(num)));
        dto.setMoDto(mo);
        dto.setLineVo(line);
        List<MPSDetailDto> mpsDetailDtos = new ArrayList<>();
        if (CollectionUtil.isNotBlank(flowDto.getDetails()) && CollectionUtil.isNotBlank(moDetails)) {
            for (MPSFlowDetailDto detail : flowDto.getDetails()) {
                for (MODetailDto moDetail : moDetails) {
                    if (detail.getId().equals(moDetail.getId())) {
                        MPSDetailDto mpsDetailDto = new MPSDetailDto();
                        BeanUtils.copyProperties(moDetail, mpsDetailDto);
                        mpsDetailDto.setId(null);
                        mpsDetailDto.setMatNum(moDetail.getMatNum().multiply(BigDecimal.valueOf(num)));
                        mpsDetailDto.setDevNo(detail.getDevNo());
                        mpsDetailDto.setDevName(detail.getDevName());
                        mpsDetailDto.setDgCode(detail.getDgCode());
                        mpsDetailDto.setDgName(detail.getDgName());
                        mpsDetailDto.setPriority(detail.getPriority());
                        mpsDetailDtos.add(mpsDetailDto);
                        break;
                    }
                }
            }
        } else {
            if (CollectionUtil.isNotBlank(moDetails)) {
                for (MODetailDto moDetail : moDetails) {
                    MPSDetailDto mpsDetailDto = new MPSDetailDto();
                    BeanUtils.copyProperties(moDetail, mpsDetailDto);
                    mpsDetailDto.setId(null);
                    mpsDetailDto.setMatNum(moDetail.getMatNum().multiply(BigDecimal.valueOf(num)));
                    mpsDetailDtos.add(mpsDetailDto);
                }
            }
        }
        dto.setDetails(mpsDetailDtos);
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public <T extends MPSDto> List<T> mpsFlowComplete(List<T> dtos) {
        if (CollectionUtil.isBlank(dtos)) {
            return null;
        }
        MODto mo;
        try {
            mo = RemoteHandleUtils.getDataFormResponse(moFeign.selectByCode(dtos.get(0).getMoCode()));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        if (mo == null) {
            throw new BusinessException("${mo.msg.exist.no}");
        }
        if (!mo.getStatus().equals(MOStatusEnum.INIT.getCode()) && !mo.getStatus().equals(MOStatusEnum.PART.getCode())) {
            throw new BusinessException("${mo.msg.status.error}");
        }
        Integer use = 0;
        for (T dto : dtos) {
            use += dto.getNum();
        }
        mo.setSurplus(mo.getSurplus() - use);
        List<MPSDto> r = (List<MPSDto>) dtos;
        try {
            RemoteHandleUtils.getDataFormResponse(mpsFeign.addBatch(r));
            RemoteHandleUtils.getDataFormResponse(moFeign.updateSurplus(mo.getSelfCode(), mo.getSurplus()));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        return dtos;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<MPSVo> doFlow(MPSFlowDto flowDto) {
        List<MPSVo> mpsVos = flowCalc(flowDto);
        mpsFlowComplete(mpsVos);
        return mpsVos;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<MPSVo> doFlowBatch(List<MPSFlowDto> flowDtos) {
        List<MPSVo> r = new ArrayList<>();
        if (CollectionUtil.isNotBlank(flowDtos)) {
            for (MPSFlowDto flowDto : flowDtos) {
                r.addAll(doFlow(flowDto));
            }
        }
        return r;
    }
}
