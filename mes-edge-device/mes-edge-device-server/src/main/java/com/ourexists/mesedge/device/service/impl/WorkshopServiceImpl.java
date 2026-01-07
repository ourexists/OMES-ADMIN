/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.device.mapper.WorkshopMapper;
import com.ourexists.mesedge.device.model.WorkshopDto;
import com.ourexists.mesedge.device.model.WorkshopPageQuery;
import com.ourexists.mesedge.device.pojo.Equip;
import com.ourexists.mesedge.device.pojo.Workshop;
import com.ourexists.mesedge.device.service.EquipService;
import com.ourexists.mesedge.device.service.WorkshopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkshopServiceImpl extends AbstractMyBatisPlusService<WorkshopMapper, Workshop> implements WorkshopService {

    @Autowired
    private EquipService equipService;

    @Override
    public Page<Workshop> selectByPage(WorkshopPageQuery dto) {
        LambdaQueryWrapper<Workshop> qw = new LambdaQueryWrapper<Workshop>().orderByDesc(Workshop::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    public List<Workshop> queryByCodes(List<String> codes) {
        return this.list(new LambdaQueryWrapper<Workshop>().in(Workshop::getSelfCode, codes));
    }

    @Override
    public Workshop queryByCode(String code) {
        return this.getOne(new LambdaQueryWrapper<Workshop>().eq(Workshop::getSelfCode, code).last("limit 1"));
    }

    @Override
    public void addOrUpdate(WorkshopDto dto) {
        saveOrUpdate(Workshop.wrap(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        List<Workshop> workshops = this.listByIds(ids);
        if (CollectionUtil.isBlank(workshops)) {
            return;
        }
        List<String> codes = workshops.stream().map(Workshop::getCode).toList();
        List<Workshop> children = this.list(new LambdaQueryWrapper<Workshop>().in(Workshop::getPcode, codes));
        if (CollectionUtil.isNotBlank(children)) {
            StringBuilder msg = new StringBuilder();
            for (Workshop child : children) {
                msg.append(child.getName()).append(",");
            }
            throw new BusinessException("${common.msg.delete.existchild}", msg.substring(0, msg.length() - 1));
        }
        List<String> selfcodes = workshops.stream().map(Workshop::getSelfCode).toList();
        List<Equip> equips = equipService.list(new LambdaUpdateWrapper<Equip>().in(Equip::getWorkshopCode, selfcodes));
        if (CollectionUtil.isNotBlank(equips)) {
            StringBuilder msg = new StringBuilder();
            for (Equip child : equips) {
                msg.append(child.getName()).append(",");
            }
            throw new BusinessException("${common.msg.delete.existdata}", msg.substring(0, msg.length() - 1));
        }
        this.removeBatchByIds(ids);
    }

    @Override
    public List<Workshop> queryChildBySelfCode(String workshopCode) {
        return this.list(new LambdaQueryWrapper<Workshop>().eqSql(Workshop::getPcode, "select code from t_workshop where self_code='" + workshopCode + "'"));
    }
}
