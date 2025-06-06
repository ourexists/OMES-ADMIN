/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.depart.*;
import com.ourexists.mesedge.ucenter.feign.DepartFeign;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "部门")
@RestController
@RequestMapping("/depart")
public class DepartController {

    @Autowired
    private DepartFeign departFeign;

    @Operation(summary = "分页查询")
    @PostMapping("/selectByPage")
    public JsonResponseEntity<List<DepartTreeNode>> selectByPage(@RequestBody DepartPageQuery pageQuery) {
        return departFeign.selectByPage(pageQuery);
    }

    @Operation(summary = "新增部门")
    @PostMapping("/add")
    public JsonResponseEntity<Boolean> add(@RequestBody @Validated DepartDto userGroupDto) {
        return departFeign.add(userGroupDto);
    }

    @Operation(summary = "更新部门")
    @PostMapping("/modify")
    public JsonResponseEntity<Boolean> modify(@RequestBody @Validated DepartModifyDto modifyDto) {
        return departFeign.modify(modifyDto);
    }

    @Operation(summary = "为部门添加用户")
    @PostMapping("/addUserToUserGroup")
    public JsonResponseEntity<Boolean> addUserToUserGroup(@RequestBody @Validated AccDepartBind2Dto dto) {
        return departFeign.addUserToUserGroup(dto);
    }

    @Operation(summary = "分配部门给用户")
    @PostMapping("/assignUserGroupsToUsers")
    public JsonResponseEntity<Boolean> assignUserGroupsToUsers(@RequestBody @Validated AccDepartBindDto dto) {
        return departFeign.assignUserGroupsToUsers(dto);
    }

    @Operation(summary = "删除部门")
    @PostMapping("/delete")
    public JsonResponseEntity<Boolean> delete(@RequestParam String id) {
        return departFeign.delete(id);
    }

    @Operation(summary = "通过id查询某节点下的部门树")
    @PostMapping("/selectDepartTreeById")
    public JsonResponseEntity<DepartTreeNode> selectDepartTreeById(@RequestParam String rootId) {
        return departFeign.selectDepartTreeById(rootId);
    }

    @Operation(summary = "通过code查询某节点下的部门树")
    @PostMapping("/selectDepartTreeByCode")
    public JsonResponseEntity<DepartTreeNode> selectDepartTreeByCode(@RequestParam String code) {
        return departFeign.selectDepartTreeByCode(code);
    }

    @Operation(summary = "查询所有部门树")
    @PostMapping("/selectAllDepartTree")
    public JsonResponseEntity<List<DepartTreeNode>> selectAllDepartTree() {
        return departFeign.selectAllDepartTree();
    }
}
