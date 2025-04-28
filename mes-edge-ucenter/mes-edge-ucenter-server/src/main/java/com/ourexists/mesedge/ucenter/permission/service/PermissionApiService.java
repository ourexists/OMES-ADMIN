/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.permission.service;


import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.ucenter.permission.PermissionApiDetailDto;
import com.ourexists.mesedge.ucenter.permission.pojo.PermissionApi;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface PermissionApiService extends IMyBatisPlusService<PermissionApi> {

    void assignApiToPermission(String permissionId, List<PermissionApiDetailDto> permissionApiDetailDtoList);

    List<PermissionApi> selectByPermissionId(String permissionId);

    void clearApiPermission(String permissionId, boolean isResetCache);
}
