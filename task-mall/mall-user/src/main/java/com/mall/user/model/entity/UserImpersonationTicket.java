package com.mall.user.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/** 后台模拟登录票据与审计记录。ticket 只保存哈希，用于一次性兑换与追踪。 */
@Data
@Table(value = "user_impersonation_ticket", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class UserImpersonationTicket extends BaseEntity<Long> {

    private String ticketNo;

    private String ticketHash;

    private Long targetUserId;

    private String adminAccount;

    private String adminIp;

    private String userAgent;

    private String status;

    private Long expiresAt;

    private Long consumedAt;

    private String failReason;
}