package com.ourexists.omes.device.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;

/**
 * 设备实时态持久化行（与 {@link EquipRealtime} 对应，payload 存完整快照）。
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "t_equip_realtime", autoResultMap = true)
public class EquipRealtimeRecord {

    @TableId
    private String id;

    private String tenantId;

    private String selfCode;

    private String gwId;

    private Integer onlineState;

    private Integer runState;

    private Integer alarmState;

    private Date onlineChangeTime;

    private Date runChangeTime;

    private Date alarmChangeTime;

    private String workshopCode;

    private String name;

    @TableField("equip_time")
    private Date equipTime;

    private Integer alarmLevel;

    @TableField(value = "payload", typeHandler = JacksonTypeHandler.class, jdbcType = JdbcType.OTHER)
    private EquipRealtime payload;
}
