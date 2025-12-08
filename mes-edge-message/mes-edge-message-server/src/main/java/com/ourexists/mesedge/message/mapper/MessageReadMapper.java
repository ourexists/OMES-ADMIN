/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.mesedge.message.model.query.MessagePageQuery;
import com.ourexists.mesedge.message.pojo.Message;
import com.ourexists.mesedge.message.pojo.MessageRead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
@Mapper
public interface MessageReadMapper extends BaseMapper<MessageRead> {
}
