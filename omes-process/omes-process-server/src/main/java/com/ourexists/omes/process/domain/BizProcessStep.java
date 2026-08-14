package com.ourexists.omes.process.domain;

import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Schema(description = "工序（引擎编译入参）")
public class BizProcessStep extends EraEntity {

    @Schema(description = "工艺ID")
    private String processId;

    @Schema(description = "工序号（如 5、10、15）")
    private Integer stepNo;

    @Schema(description = "工序编码（Route Step，如 MIXING、FORMING）")
    private String stepCode;

    @Schema(description = "工序名称")
    private String stepName;

    @Schema(description = "工序内容")
    private String stepContent;

    @Schema(description = "工序执行脚本 JSON（单步 type/mode，与 stepContent 分离）")
    private String stepScript;

    @Schema(description = "流程引擎编译配置 JSON（落库时生成，执行时直接加载）")
    private String stepEngineConfig;

    @Schema(description = "工序动态参数 JSON（如 segments，编译引擎配置时注入配方模板）")
    private String params;

    @Schema(description = "排序")
    private Integer sortOrder;
}
