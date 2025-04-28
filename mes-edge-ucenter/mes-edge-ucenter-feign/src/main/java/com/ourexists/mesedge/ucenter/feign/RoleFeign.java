/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.role.AccRoleBindDto;
import com.ourexists.mesedge.ucenter.role.DepartRoleBindDto;
import com.ourexists.mesedge.ucenter.role.RoleDto;
import com.ourexists.mesedge.ucenter.role.RolePageQuery;
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
//@Tag(name = "角色")
//@RestController
//@RequestMapping("/role")
public interface RoleFeign {

//    @Operation(summary = "分页查询")
//    @PostMapping("/selectByPage")
    JsonResponseEntity<List<RoleDto>> selectByPage(@RequestBody RolePageQuery pageQuery);

//    @Operation(summary = "新增更新角色")
//    @PostMapping("/addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@RequestBody @Validated RoleDto dto);

//    @Operation(summary = "删除角色")
//    @PostMapping("/delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

//    @Operation(summary = "用户持有角色(仅账户)")
//    @GetMapping("/selectRoleWhichAccHoldOnly")
    JsonResponseEntity<List<RoleDto>> selectRoleWhichAccHoldOnly(@RequestParam String accId);

//    @Operation(summary = "用户持有角色(全系统)")
//    @GetMapping("/selectRoleWhichAccHold")
    JsonResponseEntity<List<RoleDto>> selectRoleWhichAccHold(@RequestParam String accId);

//    @Operation(summary = "部门持有角色")
//    @GetMapping("/selectRoleWhichDepartHold")
    JsonResponseEntity<List<RoleDto>> selectRoleWhichDepartHold(@RequestParam String departId);

//    @Operation(summary = "绑定部门角色")
//    @PostMapping("/bindDepart")
    JsonResponseEntity<Boolean> bindDepart(@Validated @RequestBody DepartRoleBindDto dto);

//    @Operation(summary = "绑定账户角色")
//    @PostMapping("/bindAcc")
    JsonResponseEntity<Boolean> bindAcc(@Validated @RequestBody AccRoleBindDto dto);
}
