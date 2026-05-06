/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.line.service;

import com.ourexists.omes.line.model.ResetLineTFDto;

public interface LineFlowService {

    void resetLineTF(ResetLineTFDto dto);

    void downloadS7(String lineId, String serverName);
}
