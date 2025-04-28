/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.sync.manager;

import lombok.Getter;

public class SyncException extends Exception {

    @Getter
    private String point;

    public SyncException(String point) {
        this.point = point;
    }

    public SyncException(String point, String message) {
        super(message);
        this.point = point;
    }
}
