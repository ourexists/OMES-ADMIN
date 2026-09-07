/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.mo.model.MODetailDto;
import com.ourexists.omes.mo.model.MODto;
import com.ourexists.omes.mo.enums.MOStatusEnum;
import com.ourexists.omes.mo.mapper.MOMapper;
import com.ourexists.omes.mo.pojo.MO;
import com.ourexists.omes.mo.pojo.MODetail;
import com.ourexists.omes.mo.model.query.MOPageQuery;
import com.ourexists.omes.mo.service.MODetailService;
import com.ourexists.omes.mo.service.MOService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MOServiceImpl extends AbstractMyBatisPlusService<MOMapper, MO> implements MOService {

    @Autowired
    private MODetailService detailService;

    @Override
    public Page<MO> selectByPage(MOPageQuery dto) {
        LambdaQueryWrapper<MO> qw = new LambdaQueryWrapper<MO>()
                .eq(StringUtils.isNotEmpty(dto.getSelfCode()), MO::getSelfCode, dto.getSelfCode())
                .eq(StringUtils.isNotEmpty(dto.getProductCode()), MO::getProductCode, dto.getProductCode())
                .eq(dto.getStatus() != null, MO::getStatus, dto.getStatus())
                .like(StringUtils.isNotEmpty(dto.getProductName()), MO::getProductName, dto.getProductName())
                .orderByDesc(MO::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    public List<MO> selectByCodes(List<String> codes) {
        return this.list(new LambdaQueryWrapper<MO>().in(MO::getSelfCode, codes));
    }

    @Override
    public MO selectByCode(String code) {
        return this.getOne(new LambdaQueryWrapper<MO>().eq(MO::getSelfCode, code));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(MODto dto) {
        BigDecimal total = BigDecimal.ZERO;
        if (CollectionUtil.isNotBlank(dto.getDetailDtoList())) {
            for (MODetailDto moDetailDto : dto.getDetailDtoList()) {
                total = total.add(moDetailDto.getMatNum());
            }
        }
        if (dto.getSurplus() == null) {
            dto.setSurplus(dto.getNum());
        }
        dto.setWeight(total);
        saveOrUpdate(MO.wrap(dto));
        if (CollectionUtil.isNotBlank(dto.getDetailDtoList())) {
            detailService.remove(new LambdaQueryWrapper<MODetail>().eq(MODetail::getMcode, dto.getSelfCode()));
            detailService.saveBatch(MODetail.wrap(dto.getDetailDtoList(), dto.getSelfCode()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBatch(List<MODto> moDtos) {
        saveBatch(MO.wrap(moDtos));
        List<MODetail> moDetails = new ArrayList<>();
        for (MODto moDto : moDtos) {
            List<MODetailDto> moDetailDtos = moDto.getDetailDtoList();
            if (CollectionUtil.isNotBlank(moDetailDtos)) {
                moDetails.addAll(MODetail.wrap(moDetailDtos, moDto.getSelfCode()));
            }
        }
        detailService.saveBatch(moDetails);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        // 取消语义请走 /mo/adjust(CANCEL_*)。此处仅允许清理 INIT 草稿（无子计划时由编排层校验）。
        List<MO> mos = this.listByIds(ids);
        if (CollectionUtil.isBlank(mos)) {
            return;
        }
        for (MO mo : mos) {
            if (mo.getStatus() == null || !mo.getStatus().equals(MOStatusEnum.INIT.getCode())) {
                throw new com.ourexists.era.framework.core.exceptions.BusinessException("${mo.msg.delete.only.init}");
            }
        }
        List<String> codes = mos.stream().map(MO::getSelfCode).collect(Collectors.toList());
        this.removeByIds(ids);
        detailService.remove(new LambdaQueryWrapper<MODetail>().in(MODetail::getMcode, codes));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(List<String> moCodes, MOStatusEnum moStatusEnum) {
        update(new LambdaUpdateWrapper<MO>().set(MO::getStatus, moStatusEnum.getCode()).in(MO::getSelfCode, moCodes));
    }

    @Override
    public void updateSurplus(String code, int surplus) {
        this.baseMapper.updateSurplus(code, surplus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateExecTime(String code, java.util.Date execTime) {
        this.update(new LambdaUpdateWrapper<MO>()
                .set(MO::getExecTime, execTime)
                .eq(MO::getSelfCode, code)
                .ne(MO::getStatus, MOStatusEnum.CANCEL.getCode()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLineCode(String code, String lineCode) {
        this.update(new LambdaUpdateWrapper<MO>()
                .set(MO::getLineCode, lineCode)
                .eq(MO::getSelfCode, code)
                .ne(MO::getStatus, MOStatusEnum.CANCEL.getCode()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNumAndSurplus(String code, int num, int surplus) {
        this.baseMapper.updateNumAndSurplus(code, num, surplus);
    }
}
