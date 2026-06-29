CREATE TABLE `mission_info`
(
    `id`          bigint  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `mission_id`     bigint  NOT NULL COMMENT '全局唯一ID',
    `mission_name` varchar(100) NOT NULL COMMENT '任务名称',
    `mission_desc` VARCHAR,
    `base_reward` COMMENT '基础奖励',
    `earnings_Model` COMMENT '收益模式（0-固定收益，1-VIP加成）',

    `email`       varchar(150)     DEFAULT NULL COMMENT '邮箱',
    `nickname`    varchar(50)      DEFAULT NULL COMMENT '昵称',
    `invite_code` varchar(20) NULL DEFAULT '' COMMENT '邀请码',
    `vip_level`   tinyint NOT NULL DEFAULT 0 COMMENT 'VIP等级',
    `inviter`     bigint           DEFAULT NULL COMMENT '邀请人user_id',
    `status`      tinyint NOT NULL DEFAULT 1 COMMENT '用户状态（1-正常，2-冻结）',
    `create_time` bigint           DEFAULT NULL COMMENT '数据创建时间',
    `update_time` bigint           DEFAULT NULL COMMENT '数据修改时间',
    `creator`     varchar(50)      DEFAULT '' COMMENT '数据创建者',
    `updater`     varchar(50)      DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_invite_code` (`invite_code`),
    KEY           `idx_inviter` (`inviter_user`)
) ENGINE=InnoDB COMMENT='任务信息主表';

CREATE TABLE `user_account`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`       bigint       NOT NULL COMMENT '全局唯一用户ID',
    `account`       varchar(50)  NOT NULL COMMENT '用户名',
    `password_hash` varchar(256) NOT NULL COMMENT '登录密码',
    `create_time`   bigint      DEFAULT NULL COMMENT '数据创建时间',
    `update_time`   bigint      DEFAULT NULL COMMENT '数据修改时间',
    `creator`       varchar(50) DEFAULT '' COMMENT '数据创建者',
    `updater`       varchar(50) DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_user_name` (`user_name`),
    KEY             `idx_user_pwsd` (`user_name`, `password_hash`)
) ENGINE=InnoDB COMMENT='用户账号密码表';

CREATE TABLE `user_freeze`
(
    `id`            bigint  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`       bigint  NOT NULL COMMENT '全局唯一用户ID',
    `freeze_mode`   tinyint NOT NULL COMMENT '冻结模式（0-临时冻结，1-永久冻结）',
    `freeze_reason` VARCHAR(200) NULL DEFAULT '' COMMENT '冻结原因',
    `expire_time`   bigint  NOT NULL COMMENT '冻结截止时间',
    `status`        tinyint NOT NULL COMMENT '状态（0-冻结中，1-已解冻）',
    `create_time`   bigint      DEFAULT NULL COMMENT '数据创建时间',
    `update_time`   bigint      DEFAULT NULL COMMENT '数据修改时间',
    `creator`       varchar(50) DEFAULT '' COMMENT '数据创建者',
    `updater`       varchar(50) DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    KEY             `idx_user_id` (`user_id`, `freeze_mode`, `expire_time`)
) ENGINE=InnoDB COMMENT='用户冻结信息表';

CREATE TABLE `user_invite_relation`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     bigint      NOT NULL COMMENT '全局唯一用户ID',
    `inviter_id`  bigint      NOT NULL COMMENT '邀请用户ID',
    `invite_code` varchar(16) NOT NULL COMMENT '邀请码',
    `create_time` bigint      DEFAULT NULL COMMENT '数据创建时间',
    `update_time` bigint      DEFAULT NULL COMMENT '数据修改时间',
    `creator`     varchar(50) DEFAULT '' COMMENT '数据创建者',
    `updater`     varchar(50) DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY           `idx_inviter` (`inviter_id`)
) ENGINE=InnoDB COMMENT='用户邀请关系';

-- 应该放在钱包中心
