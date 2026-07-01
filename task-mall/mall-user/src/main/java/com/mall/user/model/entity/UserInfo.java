package com.mall.user.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table(value = "user_info", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class UserInfo extends BaseEntity<Long> {

    /**
     * 全局唯一用户ID
     */
    private Long userId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * VIP等级
     */
    private Integer vipLevel;

    /**
     * 邀请人user_id
     */
    private Long inviter;

    /**
     * 用户状态（1-正常，2-冻结）
     */
    private Integer status;
}
