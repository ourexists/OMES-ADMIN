/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FunMapper {

    @Select("SELECT create_table_month(#{tableName}, #{per}) from DUAL")
    String createTableMonth(String tableName, int per);
}
