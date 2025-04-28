/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.constants.CommonConstant;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.ucenter.depart.*;
import com.ourexists.mesedge.ucenter.depart.pojo.Depart;
import com.ourexists.mesedge.ucenter.depart.service.DepartService;
import com.ourexists.mesedge.ucenter.depart.service.DepartUsersService;
import com.ourexists.mesedge.ucenter.feign.DepartFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
//@Slf4j
//@Tag(name =  "部门")
//@RestController
//@RequestMapping("/depart")
@Component
public class DepartViewer implements DepartFeign {

    @Autowired
    private DepartService departService;

    @Autowired
    private DepartUsersService departUsersService;

//    @Operation(summary = "分页查询")
//    @PostMapping("/selectByPage")
    public JsonResponseEntity<List<DepartTreeNode>> selectByPage(@RequestBody DepartPageQuery pageQuery) {
        if (CommonConstant.SYSTEM_TENANT.equals(pageQuery.getTenantId())) {
            UserContext.getTenant().setSkipMain(false);
        }
        Page<Depart> page = departService.selectByPage(pageQuery);
        return JsonResponseEntity.success(Depart.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

//    @Operation(summary = "新增部门")
//    @PostMapping("/add")
    public JsonResponseEntity<Boolean> add(@RequestBody @Validated DepartDto userGroupDto) {
        userGroupDto.setCode(UserContext.getTenant().getTenantId() + userGroupDto.getCode());
        departService.add(userGroupDto);
        return JsonResponseEntity.success(true);
    }

//    @Operation(summary = "更新部门")
//    @PostMapping("/modify")
    public JsonResponseEntity<Boolean> modify(@RequestBody @Validated DepartModifyDto modifyDto) {
        departService.modify(modifyDto);
        return JsonResponseEntity.success(true);
    }

//    @Operation(summary = "为部门添加用户")
//    @PostMapping("/addUserToUserGroup")
    public JsonResponseEntity<Boolean> addUserToUserGroup(@RequestBody @Validated AccDepartBind2Dto dto) {
        departUsersService.addUserToUserGroup(dto.getDepartId(), dto.getAccIds());
        return JsonResponseEntity.success(true);
    }

//    @Operation(summary = "分配部门给用户")
//    @PostMapping("/assignUserGroupsToUsers")
    public JsonResponseEntity<Boolean> assignUserGroupsToUsers(@RequestBody @Validated AccDepartBindDto dto) {
        departUsersService.assignUserGroupsToUsers(dto.getAccId(), dto.getDepartIds());
        return JsonResponseEntity.success(true);
    }

//    @Operation(summary = "删除部门")
//    @PostMapping("/delete")
    public JsonResponseEntity<Boolean> delete(@RequestParam String id) {
        departService.removeById(id);
        return JsonResponseEntity.success(true);
    }

//    @Operation(summary = "通过id查询某节点下的部门树")
//    @PostMapping("/selectDepartTreeById")
    public JsonResponseEntity<DepartTreeNode> selectDepartTreeById(@RequestParam String rootId) {
        return JsonResponseEntity.success(departService.selectDepartTreeById(rootId));
    }

//    @Operation(summary = "通过code查询某节点下的部门树")
//    @PostMapping("/selectDepartTreeByCode")
    public JsonResponseEntity<DepartTreeNode> selectDepartTreeByCode(@RequestParam String code) {
        return JsonResponseEntity.success(departService.selectDepartTreeByCode(code));
    }

//    @Operation(summary = "查询所有部门树")
//    @PostMapping("/selectAllDepartTree")
    public JsonResponseEntity<List<DepartTreeNode>> selectAllDepartTree() {
        UserContext.getTenant().setSkipMain(false);
        return JsonResponseEntity.success(departService.selectAllDepartTree());
    }
}
