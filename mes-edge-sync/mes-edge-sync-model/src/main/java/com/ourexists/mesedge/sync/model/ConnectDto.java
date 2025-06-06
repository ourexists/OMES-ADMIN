/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class ConnectDto {

    protected String id;

    protected String createdBy;

    protected String createdId;

    protected Date createdTime;

    protected String updatedBy;

    protected String updatedId;

    protected Date updatedTime;
    /**
     * 服务名
     */
    private String serverName;

    /**
     * ip
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    private String suffix;

    private String protocol;

    private Boolean enabled;
}
