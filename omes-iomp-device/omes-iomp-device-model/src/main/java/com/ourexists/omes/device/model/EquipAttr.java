package com.ourexists.omes.device.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class EquipAttr {

    private String name;

    private String map;

    private String value;

    private String unit;

    private Boolean needCollect = false;

    /** 是否开启波动检测 */
    private Boolean fluctuationEnabled = true;

    /** 波动检测阈值比例 */
    private Double fluctuationThresholdRatio;

    /** 波动检测最小有效幅值 */
    private Double fluctuationMinDelta;

    /** 波动检测连续窗口数 */
    private Integer fluctuationConsecutiveWindows;
}
