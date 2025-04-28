/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.ucenter.feign.RoleFeign;
import com.ourexists.mesedge.ucenter.role.AccRoleBindDto;
import com.ourexists.mesedge.ucenter.role.DepartRoleBindDto;
import com.ourexists.mesedge.ucenter.role.RoleDto;
import com.ourexists.mesedge.ucenter.role.RolePageQuery;
import com.ourexists.mesedge.ucenter.role.pojo.Role;
import com.ourexists.mesedge.ucenter.role.service.AccRoleService;
import com.ourexists.mesedge.ucenter.role.service.DepartRoleService;
import com.ourexists.mesedge.ucenter.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
@Component
public class RoleViewer implements RoleFeign {

    @Autowired
    private RoleService roleService;

    @Autowired
    private DepartRoleService departRoleService;

    @Autowired
    private AccRoleService accRoleService;

    @Operation(summary = "分页查询")
    @PostMapping("/selectByPage")
    public JsonResponseEntity<List<RoleDto>> selectByPage(@RequestBody RolePageQuery pageQuery) {
        if (UserContext.getTenant() != null) {
            //主租户只查询租户本身的数据
            UserContext.getTenant().setSkipMain(false);
        }
        Page<Role> page = roleService.selectByPage(pageQuery);
        return JsonResponseEntity.success(Role.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    @Operation(summary = "新增更新角色")
    @PostMapping("/addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@RequestBody @Validated RoleDto dto) {
        roleService.addOrUpdate(dto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "删除角色")
    @PostMapping("/delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        roleService.remove(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "用户持有角色(仅账户)")
    @GetMapping("/selectRoleWhichAccHoldOnly")
    public JsonResponseEntity<List<RoleDto>> selectRoleWhichAccHoldOnly(@RequestParam String accId) {
        return JsonResponseEntity.success(Role.covert(roleService.selectRoleWhichAccHold(accId, false)));
    }

    @Operation(summary = "用户持有角色(全系统)")
    @GetMapping("/selectRoleWhichAccHold")
    public JsonResponseEntity<List<RoleDto>> selectRoleWhichAccHold(@RequestParam String accId) {
        return JsonResponseEntity.success(Role.covert(roleService.selectRoleWhichAccHold(accId, true)));
    }

    @Operation(summary = "部门持有角色")
    @GetMapping("/selectRoleWhichDepartHold")
    public JsonResponseEntity<List<RoleDto>> selectRoleWhichDepartHold(@RequestParam String departId) {
        return JsonResponseEntity.success(Role.covert(roleService.selectRoleWhichDepartHold(departId)));
    }

    @Operation(summary = "绑定部门角色")
    @PostMapping("/bindDepart")
    public JsonResponseEntity<Boolean> bindDepart(@Validated @RequestBody DepartRoleBindDto dto) {
        departRoleService.establishRelation(dto.getDepartId(), dto.getRoleIds());
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "绑定账户角色")
    @PostMapping("/bindAcc")
    public JsonResponseEntity<Boolean> bindAcc(@Validated @RequestBody AccRoleBindDto dto) {
        accRoleService.establishRelation(dto.getAccId(), dto.getRoleIds());
        return JsonResponseEntity.success(true);
    }
}
