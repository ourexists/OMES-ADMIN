/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;


@Schema
@Getter
@Setter
@Accessors(chain = true)
public class EquipRecordCountQuery {

    @NotNull
    private Date startDate;

    @NotNull
    private Date endDate;

    @NotBlank
    private String sn;

    /**
     * 若查询结束时间晚于当前时刻则截断为当前时刻，避免统计/甘特查询到未来区间。
     */
    public void capEndDateToNow() {
        if (endDate == null) {
            return;
        }
        Date now = new Date();
        if (endDate.after(now)) {
            this.endDate = now;
        }
    }
}
