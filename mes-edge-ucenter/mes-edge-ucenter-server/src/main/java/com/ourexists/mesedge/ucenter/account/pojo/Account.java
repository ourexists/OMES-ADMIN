/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.account.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.DateUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.ucenter.account.AccAddDto;
import com.ourexists.mesedge.ucenter.account.AccVo;
import com.ourexists.mesedge.ucenter.enums.AccStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/6 14:44
 * @since 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("p_ucenter_acc")
public class Account extends MainEntity {

    /**
     * 账户名
     */
    private String accName;

    /**
     * 平台
     */
    private String platform;

    /**
     * 账户密码
     */
    private String password;

    /**
     * 账户昵称
     */
    private String nickName;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 账户状态
     */
    private Integer status;

    /**
     * 账户入驻时间
     */
    private Date settledTime;

    /**
     * 账户过期时间
     */
    private Date expireTime;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 出生年月
     */
    private Date birthDay;

    /**
     * 用户性别
     */
    private Integer sex;

    /**
     * 账户手机号
     */
    private String mobile;

    /**
     * 账户邮箱
     */
    private String email;

    /**
     * 初始状态
     */
    private Boolean init;

    private String source;

    private String sourceId;

    private String unionId;

    private String avatarUrl;

    private String country;

    private String province;

    private String city;

    private String language;

    private Integer perfection;

    public static <T extends AccAddDto> Account warp(T source) {
        Account target = new Account();
        BeanUtils.copyProperties(source, target);
        target.setStatus(AccStatusEnum.COMMON.getCode());
        if (target.getSettledTime() == null) {
            target.setSettledTime(new Date());
        }
        if (target.getExpireTime() == null) {
            target.setExpireTime(DateUtil.getWarpYear(target.getSettledTime(), 100));
        }
        return target;
    }

    public static AccVo covert(Account source) {
        return covert(source, true);
    }

    public static AccVo covert(Account source, boolean hiddenPass) {
        if (source == null) {
            return null;
        }
        AccVo target = new AccVo();
        BeanUtils.copyProperties(source, target);
        target.setStatusDesc(AccStatusEnum.valueof(source.getStatus()).getDesc());
        if (hiddenPass) {
            target.setPassword("****************");
        }
        return target;
    }

    public static List<AccVo> covert(List<Account> sources) {
        List<AccVo> targets = new ArrayList<>();
        if (!CollectionUtils.isEmpty(sources)) {
            for (Account source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }
}
