/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.device.model.DeviceTreeNode;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface DeviceFeign {

    JsonResponseEntity<List<DeviceTreeNode>> selectByDgIdAndStatus(@RequestParam String dgId);

    JsonResponseEntity<Boolean> isUseMat(@RequestParam List<String> matCodes);
}
