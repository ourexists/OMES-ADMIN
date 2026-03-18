/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.inspection.controller;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.inspection.feign.InspectPersonFeign;
import com.ourexists.omes.inspection.model.InspectPersonDto;
import com.ourexists.omes.inspection.model.InspectPersonPageQuery;
import com.ourexists.omes.ucenter.account.AccRegisterDto;
import com.ourexists.omes.ucenter.account.AccVo;
import com.ourexists.omes.ucenter.feign.AccountFeign;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 巡检人员管理
 */
@Tag(name = "巡检人员")
@RestController
@RequestMapping("/inspection/person")
public class InspectPersonController {

    @Autowired
    private InspectPersonFeign feign;
    @Autowired
    private AccountFeign accountFeign;

    @Operation(summary = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<InspectPersonDto>> selectByPage(@RequestBody InspectPersonPageQuery query) {
        return feign.selectByPage(query);
    }

    @Operation(summary = "新增或修改（支持关联已有账户或同步添加账户）")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectPersonSaveRequest request) {
        InspectPersonDto dto = request.getPerson();
        if (dto == null) {
            throw new BusinessException("人员信息不能为空");
        }
        if (Boolean.TRUE.equals(request.getSyncAccount()) && request.getAccountInfo() != null) {
            // 同步添加账户：先创建账户，再设置 person 的 accountId
            AccRegisterDto accDto = new AccRegisterDto();
            accDto.setAccName(request.getAccountInfo().getAccName());
            accDto.setUserName(request.getPerson().getName());
            accDto.setPassword(request.getAccountInfo().getPassword());
            accDto.setNickName(request.getAccountInfo().getNickName() != null ? request.getAccountInfo().getNickName() : dto.getName());
            accDto.setPlatform("mes-app");
            if (UserContext.getTenant() != null && UserContext.getTenant().getTenantId() != null) {
                accDto.setTenantId(UserContext.getTenant().getTenantId());
            }
            if (request.getAccountInfo().getAccRole() != null) {
                accDto.setAccRole(request.getAccountInfo().getAccRole());
            }
            try {
                String accId = RemoteHandleUtils.getDataFormResponse(accountFeign.register(accDto));
                dto.setAccountId(accId);
                dto.setAccountName(accDto.getNickName());
            } catch (EraCommonException e) {
                throw new BusinessException(e.getMessage());
            }
        } else if (request.getAccountId() != null) {
            // 关联已有账户：可为空表示不关联
            if (StringUtils.hasText(request.getAccountId())) {
                dto.setAccountId(request.getAccountId());
                try {
                    AccVo acc = RemoteHandleUtils.getDataFormResponse(accountFeign.selectById(request.getAccountId()));
                    dto.setAccountName(acc.getNickName() != null ? acc.getNickName() : acc.getAccName());
                } catch (EraCommonException e) {
                    throw new BusinessException(e.getMessage());
                }
            } else {
                dto.setAccountId(null);
                dto.setAccountName(null);
            }
        }
        return feign.addOrUpdate(dto);
    }

    @Operation(summary = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }

    @Operation(summary = "根据ID查询")
    @GetMapping("selectById")
    public JsonResponseEntity<InspectPersonDto> selectById(@RequestParam String id) {
        return feign.selectById(id);
    }
}
