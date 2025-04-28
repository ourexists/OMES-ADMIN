/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.account;

import com.ourexists.era.framework.core.constants.RegexConstants;
import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 * @author pengcheng
 * @date 2022/4/6 18:04
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class AccAddDto extends BaseDto {

    private static final long serialVersionUID = 200000L;

    @Schema(description = "账户名")
    @NotBlank(message = "请输入账户名！")
    private String accName;

    @Schema(description = "账户密码", required = true)
    @NotBlank(message = "请设置账户密码！")
    @Pattern(regexp = RegexConstants.STORAGE_PASSWORD, message = "密码必须包含大小写字母、数字、特殊字符，长度在8-16之间")
    private String password;

    @Schema(description = "所属平台")
    private String platform;

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

    @Schema(description = "来源")
    private String source;

    @Schema(description = "来源id")
    private String sourceId;
}
