package com.ourexists.omes.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.omes.device.pojo.EquipRealtimeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EquipRealtimeMapper extends BaseMapper<EquipRealtimeRecord> {

    int upsert(@Param("row") EquipRealtimeRecord row);

    /** 事务级尝试锁，释放随事务结束 */
    @Select("SELECT pg_try_advisory_xact_lock(#{k1}, #{k2})")
    Boolean tryAdvisoryXactLock(@Param("k1") int k1, @Param("k2") int k2);

    /** 事务级阻塞锁，释放随事务结束 */
    @Select("SELECT pg_advisory_xact_lock(#{k1}, #{k2})")
    void advisoryXactLock(@Param("k1") int k1, @Param("k2") int k2);
}
