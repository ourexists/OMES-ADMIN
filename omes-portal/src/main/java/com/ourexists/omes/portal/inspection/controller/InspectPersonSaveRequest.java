/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.inspection.controller;

import com.ourexists.omes.inspection.model.InspectPersonDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 巡检人员保存请求：人员信息 + 关联方式（关联已有账户 / 同步添加账户）
 */
@Schema(description = "巡检人员保存请求")
@Getter
@Setter
public class InspectPersonSaveRequest {

    @Schema(description = "巡检人员信息")
    private InspectPersonDto person;

    @Schema(description = "true=同步添加账户并关联，false 或不传=仅关联")
    private Boolean syncAccount;

    @Schema(description = "关联已有账户时传入的账户ID")
    private String accountId;

    @Schema(description = "同步添加账户时传入的账户信息（accName、password、nickName、accRole）")
    private SyncAccountInfo accountInfo;

    @Getter
    @Setter
    public static class SyncAccountInfo {
        @Schema(description = "账户名（登录用）", requiredMode = Schema.RequiredMode.REQUIRED)
        private String accName;
        @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
        private String password;
        @Schema(description = "昵称/姓名")
        private String nickName;
        @Schema(description = "租户角色")
        private String accRole;
    }
}
