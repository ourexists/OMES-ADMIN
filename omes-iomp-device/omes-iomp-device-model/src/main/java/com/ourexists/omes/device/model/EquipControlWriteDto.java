package com.ourexists.omes.device.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class EquipControlWriteDto {

    @NotEmpty
    private String equipId;

    /** PLC/设备 可写地址 */
    @NotEmpty
    private String address;

    /** 写入值 */
    @NotNull
    private Object value;
}
