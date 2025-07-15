/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.viewer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.mat.feign.MATFeign;
import com.ourexists.mesedge.mat.model.MaterialDto;
import com.ourexists.mesedge.mat.model.query.MaterialPageQuery;
import com.ourexists.mesedge.mat.pojo.MAT;
import com.ourexists.mesedge.mat.service.BOMDService;
import com.ourexists.mesedge.mat.service.MATService;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

//@Tag(name = "物料")
//@RestController
//@RequestMapping("/mat")
@Component
public class MATViewer implements MATFeign {

    @Resource
    private MATService matService;

    @Resource
    private BOMDService bomdService;


    //    @Operation(summary = "分页", description = "")
//    @PostMapping("selectByPage")
    public JsonResponseEntity<List<MaterialDto>> selectByPage(@RequestBody MaterialPageQuery dto) {
        Page<MAT> page = matService.selectByPage(dto);
        return JsonResponseEntity.success(MAT.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    //    @Operation(summary = "新增或修改根据id", description = "")
//    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody MaterialDto materialDto) {
        try {
            matService.saveOrUpdate(MAT.wrap(materialDto));
        } catch (DataAccessException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SQLIntegrityConstraintViolationException) {
                throw new BusinessException("${valid.code.duplicate}");
            }
        }
        return JsonResponseEntity.success(true);
    }

    //    @Operation(summary = "删除", description = "")
//    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        if (bomdService.existMat(idsDto.getIds())) {
            throw new BusinessException("${common.msg.date.use}");
        }
        matService.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<List<MaterialDto>> selectByCodes(@RequestBody IdsDto idsDto) {
        List<MAT> mats = matService.list(new LambdaQueryWrapper<MAT>().in(MAT::getSelfCode, idsDto.getIds()));
        return JsonResponseEntity.success(MAT.covert(mats));
    }

    @Override
    public JsonResponseEntity<List<MaterialDto>> selectByIds(@RequestBody IdsDto idsDto) {
        return JsonResponseEntity.success(MAT.covert(matService.listByIds(idsDto.getIds())));
    }

}
