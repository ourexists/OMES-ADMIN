package com.ourexists.omes.process.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@TableName("r_biz_process_mold")
@Schema(description = "工艺压模图号")
public class BizProcessMold extends EraEntity {

    @Schema(description = "工艺ID")
    private String processId;

    @Schema(description = "压模图号")
    private String moldDrawingNo;

    @Schema(description = "压模槽数")
    private Integer slotCount;

    @Schema(description = "排序")
    private Integer sortOrder;
}
