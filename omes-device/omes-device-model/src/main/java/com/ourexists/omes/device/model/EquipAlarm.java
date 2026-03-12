package com.ourexists.omes.device.model;

import com.ourexists.omes.device.enums.AlarmLevelEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class EquipAlarm {

    /** 映射字段 */
    private String map;
    /** 比较类型: 0=相等, 1=大于, 2=大于等于, 3=小于, 4=小于等于, 5=范围 */
    private Integer type;
    /** 单值比较时的阈值 (类型0/1/2/3/4 时使用) */
    private String val;
    /** 范围比较时的下限 (类型5范围 时使用) */
    private String min;
    /** 范围比较时的上限 (类型5范围 时使用) */
    private String max;
    /** 报警描述 */
    private String text;
    /** 报警等级: 0=轻微, 1=一般, 2=严重, 3=故障，见 {@link AlarmLevelEnum} */
    private Integer level;
}
