package com.mall.admin.model.vo;

import lombok.Data;


@Data
public class UserInfoVO {

    /**
     * 账号
     */
    private String userName;

    /**
     * VIP等级
     */
    private Integer vipLevel;

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 上级用户账号
     */
    private String parentUserName;

    /**
     * 注册时间
     */
    private String registerTime;

    /**
     * 可用余额
     */
    private String availableAmt;

    /**
     * 冻结金额
     */
    private String freezeAmt;
}
