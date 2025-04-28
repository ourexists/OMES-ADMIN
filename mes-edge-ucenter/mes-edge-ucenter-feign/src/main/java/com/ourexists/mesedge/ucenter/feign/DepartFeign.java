/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.depart.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
//@RequestMapping("/depart")
public interface DepartFeign {

    //    @Operation(summary = "分页查询")
//    @PostMapping("/selectByPage")
    JsonResponseEntity<List<DepartTreeNode>> selectByPage(@RequestBody DepartPageQuery pageQuery);

    //    @Operation(summary = "新增部门")
//    @PostMapping("/add")
    JsonResponseEntity<Boolean> add(@RequestBody @Validated DepartDto userGroupDto);

    //    @Operation(summary ="更新部门")
//    @PostMapping("/modify")
    JsonResponseEntity<Boolean> modify(@RequestBody @Validated DepartModifyDto modifyDto);

    //    @Operation(summary ="为部门添加用户")
//    @PostMapping("/addUserToUserGroup")
    JsonResponseEntity<Boolean> addUserToUserGroup(@RequestBody @Validated AccDepartBind2Dto dto);

    //    @Operation(summary ="分配部门给用户")
//    @PostMapping("/assignUserGroupsToUsers")
    JsonResponseEntity<Boolean> assignUserGroupsToUsers(@RequestBody @Validated AccDepartBindDto dto);

    //    @Operation(summary ="删除部门")
//    @PostMapping("/delete")
    JsonResponseEntity<Boolean> delete(@RequestParam String id);

    //    @Operation(summary ="通过id查询某节点下的部门树")
//    @PostMapping("/selectDepartTreeById")
    JsonResponseEntity<DepartTreeNode> selectDepartTreeById(@RequestParam String rootId);

    //    @Operation(summary ="通过code查询某节点下的部门树")
//    @PostMapping("/selectDepartTreeByCode")
    JsonResponseEntity<DepartTreeNode> selectDepartTreeByCode(@RequestParam String code);

    //    @Operation(summary ="查询所有部门树")
//    @PostMapping("/selectAllDepartTree")
    JsonResponseEntity<List<DepartTreeNode>> selectAllDepartTree();
}
