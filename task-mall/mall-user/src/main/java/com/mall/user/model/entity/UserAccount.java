package com.mall.user.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table(value = "user_account", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class UserAccount extends BaseEntity<Long> {

    /**
     * 全局唯一用户ID（跨服务身份，登录态 loginId 即此值）
     */
    private Long userId;

    /**
     * 登录账号
     */
    private String account;

    /**
     * 登录密码 BCrypt 摘要
     */
    private String passwordHash;
}