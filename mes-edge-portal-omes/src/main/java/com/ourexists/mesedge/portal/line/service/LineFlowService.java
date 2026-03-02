/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.line.service;

import com.ourexists.mesedge.line.model.ResetLineTFDto;

public interface LineFlowService {

    void resetLineTF(ResetLineTFDto dto);

    void downloadS7(String lineId, String serverName);
}
