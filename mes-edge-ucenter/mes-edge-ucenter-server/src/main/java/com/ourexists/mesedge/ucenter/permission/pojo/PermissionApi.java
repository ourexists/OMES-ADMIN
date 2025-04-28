/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.permission.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.mesedge.ucenter.permission.PermissionApiDetailDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author pengcheng
 * @date 2022/4/15 18:54
 * @since 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("r_ucenter_permission_api")
public class PermissionApi {

    @TableId
    private String id;

    private String permissionId;

    private String serverName;

    private String path;


    public static List<PermissionApiDetailDto> covert(List<PermissionApi> sources) {
        List<PermissionApiDetailDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (PermissionApi source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static List<PermissionApiDetailDto> covert(Map<String, List<PermissionApi>> sources) {
        List<PermissionApiDetailDto> targets = new ArrayList<>();
        if (sources != null && !sources.isEmpty()) {
            for (Map.Entry<String, List<PermissionApi>> entry : sources.entrySet()) {
                for (PermissionApi permissionApi : entry.getValue()) {
                    PermissionApiDetailDto t = covert(permissionApi);
                    targets.add(t);
                }
            }
        }
        return targets;
    }

    public static PermissionApiDetailDto covert(PermissionApi source) {
        PermissionApiDetailDto target = new PermissionApiDetailDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
