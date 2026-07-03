package com.mall.admin.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/** 后台机器人配置。 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "admin_bot_config", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class AdminBotConfig extends BaseEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = -4073485450529311542L;

    @Column("bot_name")
    private String botName;

    @Column("bot_token")
    private String botToken;

    @Column("webhook_url")
    private String webhookUrl;

    private String description;

    @Column("sort_order")
    private Integer sortOrder;

    private Integer status;
}