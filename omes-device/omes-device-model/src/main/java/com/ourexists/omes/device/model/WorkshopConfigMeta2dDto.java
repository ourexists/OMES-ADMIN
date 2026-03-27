/*
 * Copyright (c) 2026.
 */
package com.ourexists.omes.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class WorkshopConfigMeta2dDto {

    private String id;

    private String workshopId;

    private WorkshopMeta2dConfigDetail meta2dConfig;
}

