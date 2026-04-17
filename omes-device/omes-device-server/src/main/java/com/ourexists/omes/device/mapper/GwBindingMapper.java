package com.ourexists.omes.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.omes.device.pojo.GwBinding;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GwBindingMapper extends BaseMapper<GwBinding> {

    @Insert("INSERT INTO r_gw_binding (equip_id, gw_id, config) VALUES (" +
            "#{equipId}, #{gwId}, CAST(#{config, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler, jdbcType=OTHER} AS jsonb)" +
            ") ON CONFLICT (equip_id) DO UPDATE SET gw_id = EXCLUDED.gw_id, config = EXCLUDED.config")
    int upsertByEquipId(GwBinding binding);
}