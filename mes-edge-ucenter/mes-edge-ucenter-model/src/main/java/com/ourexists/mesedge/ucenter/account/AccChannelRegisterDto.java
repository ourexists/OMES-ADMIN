/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.account;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * @author pengcheng
 * @date 2022/4/6 18:04
 * @since 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@Schema
public class AccChannelRegisterDto extends BaseDto {

    private static final long serialVersionUID = -6518999535108931535L;

    @Schema(description = "租户id")
    private String tenantId;

    @Schema(description = "租户角色(默认普通角色)")
    private String accRole;

    @Schema(description = "账户名")
    private String accName;

    @Schema(description = "账户昵称", required = true)
    @NotBlank(message = "请输入账户昵称！")
    private String nickName;

    @Schema(description = "用户名")
    private String userName;

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

    @Schema(description = "平台")
    private String platform;

    @Schema(description = "账户注册渠道", hidden = true)
    private String channel;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "来源id")
    private String sourceId;
}
