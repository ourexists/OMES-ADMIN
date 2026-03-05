/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.core.equip.protocol;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ProtocolConnect {

    protected String id;

    /**
     * 服务名
     */
    private String serverName;

    /**
     * ip
     */
    private String uri;

    private String topic;

    private String protocol;

    private String params;

    private Boolean enabled;

    /** 采集 cron 表达式 */
    private String collectCron;

    private Integer validType;

    private String username;

    private String password;
}
