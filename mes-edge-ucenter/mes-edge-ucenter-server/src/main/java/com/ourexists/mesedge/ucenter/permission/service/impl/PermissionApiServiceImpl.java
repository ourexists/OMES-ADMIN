/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.ucenter.permission.PermissionApiDetailDto;
import com.ourexists.mesedge.ucenter.permission.mapper.PermissionApiMapper;
import com.ourexists.mesedge.ucenter.permission.pojo.PermissionApi;
import com.ourexists.mesedge.ucenter.permission.service.PermissionApiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:20
 * @since 1.0.0
 */
@Service
public class PermissionApiServiceImpl extends AbstractMyBatisPlusService<PermissionApiMapper, PermissionApi>
        implements PermissionApiService {


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignApiToPermission(String permissionId, List<PermissionApiDetailDto> permissionApiDetailDtoList) {
        clearApiPermission(permissionId, false);
        if (CollectionUtil.isNotBlank(permissionApiDetailDtoList)) {
            List<PermissionApi> permissionApis = new ArrayList<>();
            for (PermissionApiDetailDto permissionApiDetailDto : permissionApiDetailDtoList) {
                permissionApis.add(new PermissionApi()
                        .setPermissionId(permissionId)
                        .setServerName(permissionApiDetailDto.getServerName())
                        .setPath(permissionApiDetailDto.getPath()));
            }
            this.saveBatch(permissionApis);
        }
    }

    @Override
    public List<PermissionApi> selectByPermissionId(String permissionId) {
        return this.list(new LambdaQueryWrapper<PermissionApi>().eq(PermissionApi::getPermissionId, permissionId));
    }

    @Override
    public void clearApiPermission(String permissionId, boolean isResetCache) {
        this.remove(new LambdaQueryWrapper<PermissionApi>().eq(PermissionApi::getPermissionId, permissionId));
    }
}
