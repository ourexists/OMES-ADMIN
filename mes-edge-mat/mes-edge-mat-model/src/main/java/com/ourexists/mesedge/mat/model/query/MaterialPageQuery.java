/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.model.query;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialPageQuery extends PageQuery {

    private String name;

    private String selfCode;

    private String classifyCode;

}
