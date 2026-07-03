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

/** 机器人自动回复配置。 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "admin_bot_auto_reply", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class AdminBotAutoReply extends BaseEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1736989943925298806L;

    @Column("bot_id")
    private Long botId;

    private String keyword;

    @Column("reply_content")
    private String replyContent;

    @Column("match_type")
    private Integer matchType;

    @Column("sort_order")
    private Integer sortOrder;

    private Integer status;
}