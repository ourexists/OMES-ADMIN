/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.account.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
//@RequestMapping("/acc")
public interface AccountFeign {

    //    @Operation(summary = "分页查询")
//    @PostMapping("/selectByPage")
    JsonResponseEntity<List<AccVo>> selectByPage(@RequestBody AccPageQuery pageQuery);

    //    @Operation(summary = "注册账户")
//    @PostMapping("/register")
    JsonResponseEntity<String> register(@RequestBody @Valid AccRegisterDto accDto);

    //    @Operation(summary = "账户批入驻")
//    @PostMapping("/accBathSettle")
    JsonResponseEntity<AccVo> accBathSettle(@RequestBody @Valid AccBathSettleDto accDto);

    //    @Operation(summary = "修改密码")
//    @PostMapping("/changePass")
    JsonResponseEntity<Boolean> changePass(@RequestBody @Valid AccChangePassDto accChangePassDto);

    //    @Operation(summary = "账户修改")
//    @PostMapping("/modify")
    JsonResponseEntity<Boolean> modify(@RequestBody @Valid AccModifyDto accDto);


    //    @Operation(summary = "账户删除(谨慎使用)")
//    @PostMapping("/delete")
    JsonResponseEntity<Boolean> delete(@RequestBody IdsDto idsDto);

    //    @Operation(summary = "账户冻结")
//    @GetMapping("/frozen")
    JsonResponseEntity<Boolean> frozen(@RequestParam String accId);

    //    @Operation(summary = "失效用户")
//    @GetMapping("/invalid")
    JsonResponseEntity<Boolean> invalid(@RequestParam String accId);

    //    @Operation(summary = "启用用户")
//    @GetMapping("/invoke")
    JsonResponseEntity<Boolean> invoke(@RequestParam String accId);

    //    @Operation(summary = "查询部门内的所有账户")
//    @GetMapping("/selectByDepart")
    JsonResponseEntity<List<AccVo>> selectByDepart(@RequestParam String departId);

    JsonResponseEntity<List<AccVo>> selectByRoles(@RequestBody List<String> roleIds);

    //    @Operation(summary = "通过id获取当前用户信息")
//    @GetMapping("/selectById")
    JsonResponseEntity<AccVo> selectById(@RequestParam String id);
}
