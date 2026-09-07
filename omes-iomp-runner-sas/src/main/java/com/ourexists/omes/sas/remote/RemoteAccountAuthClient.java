/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.sas.remote;

import com.ourexists.era.framework.rpc.feign.EraFeignConfiguration;
import com.ourexists.omes.ucenter.feign.AccountAuthFeign;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 远程调用 OMES 管理端 ucenter 认证账户接口。
 */
@FeignClient(
        name = "omes-admin",
        url = "${omes.remote.admin-url:http://127.0.0.1:10010}",
        configuration = {EraFeignConfiguration.class, SasInternalFeignConfiguration.class}
)
public interface RemoteAccountAuthClient extends AccountAuthFeign {
}
