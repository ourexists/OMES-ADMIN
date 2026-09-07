package com.ourexists.omes.device.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 按业务 {@code event_id}（上一段的 id，即 DTO 的 {@code prevEventId}）批量闭合区间时写入的结束时间。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipRecordEventEndPatch {

    private String sn;

    /** 待更新行的 event_id，对应入参 DTO 的 prevEventId */
    private String eventId;

    private Date endTime;
}
