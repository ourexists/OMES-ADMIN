/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.device.model.DeviceDto;
import com.ourexists.mesedge.device.model.DeviceTreeNode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface DeviceFeign {

    JsonResponseEntity<List<DeviceTreeNode>> tree(@RequestParam String dgId);

    JsonResponseEntity<List<DeviceTreeNode>> selectByDgId(@RequestParam String dgId);

    JsonResponseEntity<List<DeviceTreeNode>> selectByDgIdAndStatus(@RequestParam String dgId, @RequestParam Integer status);

    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody DeviceDto dto);

    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    JsonResponseEntity<DeviceTreeNode> selectById(@RequestParam String id);

    JsonResponseEntity<Boolean> changeStatus(String id, Integer status);

    JsonResponseEntity<Boolean> isUseMat(@RequestParam List<String> matCodes);
}
