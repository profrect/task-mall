package com.mall.admin.model.vo;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class UserInfoVO {

    /**
     * 全局唯一用户ID
     */
    private Long userId;

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
    private Long registerTime;

    /**
     * 可用余额
     */
    private BigDecimal availableAmt;

    /**
     * 冻结金额
     */
    private BigDecimal freezeAmt;
}
