/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.mesedge.sync.pojo.SyncResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface SyncResourceMapper extends BaseMapper<SyncResource> {

    @Select("select count(1)  from r_sync_resource a left join t_sync b on a.sync_id =b.id " +
            "where a.req_data=#{reqData} and b.sync_tx=#{syncTx}")
    Long existSync(String syncTx, String reqData);
}
