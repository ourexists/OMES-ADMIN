/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.mat.model.BOMCDto;
import com.ourexists.mesedge.mat.model.BOMTreeNode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface BOMCFeign {

    JsonResponseEntity<List<BOMTreeNode>> treeClassify();

    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody BOMCDto dto);

    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);
}
