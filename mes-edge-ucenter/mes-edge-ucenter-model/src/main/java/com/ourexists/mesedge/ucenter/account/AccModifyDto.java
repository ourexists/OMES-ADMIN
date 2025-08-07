/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.account;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author pengcheng
 * @date 2022/4/6 18:04
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class AccModifyDto extends BaseDto {

    private static final long serialVersionUID = -7826752531226800156L;

    @NotNull(message = "请选择要修改的账户！")
    private String id;

    @Schema(description = "账户昵称", required = true)
    @NotBlank(message = "请输入账户昵称！")
    private String nickName;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "账户入驻时间")
    private Date settledTime;

    @Schema(description = "账户过期时间")
    private Date expireTime;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "出生年月")
    private Date birthDay;

    @Schema(description = "用户性别")
    private Integer sex;

    @Schema(description = "账户手机号")
    private String mobile;

    @Schema(description = "账户邮箱")
    private String email;
}
