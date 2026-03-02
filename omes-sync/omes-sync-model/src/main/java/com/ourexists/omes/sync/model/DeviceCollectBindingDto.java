/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.sync.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class DeviceCollectBindingDto {

    protected String id;
    protected Date createdTime;
    protected Date updatedTime;

    private String connectId;
    private String equipSn;
    private String sourceKey;
    private String parseRuleJson;
    private String tenantId;
}
