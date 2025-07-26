/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.mat.feign.MCFeign;
import com.ourexists.mesedge.mat.model.MaterialClassifyDto;
import com.ourexists.mesedge.mat.model.query.MaterialClassifyPageQuery;
import com.ourexists.mesedge.mat.pojo.MC;
import com.ourexists.mesedge.mat.service.MATService;
import com.ourexists.mesedge.mat.service.MCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

//@Tag(name = "物料分类")
//@RestController
//@RequestMapping("/mc")
@Component
public class MCViewer implements MCFeign {

    @Autowired
    private MCService service;


    //    @Operation(summary = "分页", description = "")
//    @PostMapping("selectByPage")
    public JsonResponseEntity<List<MaterialClassifyDto>> selectByPage(@RequestBody MaterialClassifyPageQuery dto) {
        Page<MC> page = service.selectByPage(dto);
        return JsonResponseEntity.success(MC.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    //    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
//    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody MaterialClassifyDto dto) {
        try {
            service.saveOrUpdate(MC.wrap(dto));
        } catch (DataAccessException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SQLIntegrityConstraintViolationException) {
                throw new BusinessException("${valid.code.duplicate}");
            }
        }
        return JsonResponseEntity.success(true);
    }

    //    @Operation(summary = "删除", description = "删除")
//    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.delete(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

}
