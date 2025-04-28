/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.mo.model.MODetailDto;
import com.ourexists.mesedge.mo.model.MODto;
import com.ourexists.mesedge.mo.enums.MOStatusEnum;
import com.ourexists.mesedge.mo.mapper.MOMapper;
import com.ourexists.mesedge.mo.pojo.MO;
import com.ourexists.mesedge.mo.pojo.MODetail;
import com.ourexists.mesedge.mo.model.query.MOPageQuery;
import com.ourexists.mesedge.mo.service.MODetailService;
import com.ourexists.mesedge.mo.service.MOService;
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
        List<MO> mos = this.listByIds(ids);
        if (CollectionUtil.isBlank(mos)) {
            return;
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
}
