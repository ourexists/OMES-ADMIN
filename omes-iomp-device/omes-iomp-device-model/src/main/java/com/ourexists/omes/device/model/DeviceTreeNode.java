/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class DeviceTreeNode {

    private String id;

    private String name;

    private String selfCode;

    private String dgId;

    private BigDecimal maxCapacity;

    private String matCode;
}
