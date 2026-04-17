package com.ourexists.omes.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.omes.device.pojo.EquipCollect;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EquipCollectMapper extends BaseMapper<EquipCollect> {

    @Insert("<script>" +
            "INSERT INTO t_equip_collect (id, sn, data, time, tenant_id, created_by, created_id, created_time, updated_by, updated_id, updated_time) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(" +
            "#{item.id}, " +
            "#{item.sn}, " +
            "CAST(#{item.data, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler, jdbcType=OTHER} AS jsonb), " +
            "#{item.time}, " +
            "#{item.tenantId}, " +
            "#{item.createdBy}, " +
            "#{item.createdId}, " +
            "#{item.createdTime}, " +
            "#{item.updatedBy}, " +
            "#{item.updatedId}, " +
            "#{item.updatedTime}" +
            ")" +
            "</foreach>" +
            "</script>")
    int insertBatchWithJsonb(@Param("list") List<EquipCollect> list);
}