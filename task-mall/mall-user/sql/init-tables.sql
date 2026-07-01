CREATE TABLE `user_info`
(
    `id`          bigint  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     bigint  NOT NULL COMMENT '全局唯一用户ID',
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
    KEY           `idx_inviter` (`inviter`)
) ENGINE=InnoDB COMMENT='用户信息主表';

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
    UNIQUE KEY `uk_account` (`account`),
    KEY             `idx_account_pwd` (`account`, `password_hash`)
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

CREATE TABLE `vip_level_config`
(
    `id`           bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `level`        int            NOT NULL COMMENT 'VIP等级',
    `level_name`   varchar(50)    NOT NULL DEFAULT '' COMMENT '等级名称',
    `price`        decimal(24, 6) NOT NULL DEFAULT 0.000000 COMMENT '升级价格',
    `rebate_rate`  decimal(10, 4) NOT NULL DEFAULT 0.0000 COMMENT '返佣比例配置',
    `daily_tasks`  int            NOT NULL DEFAULT 0 COMMENT '每日任务数量配置',
    `benefits`     varchar(1000)  NOT NULL DEFAULT '' COMMENT '权益说明，逗号或换行分隔',
    `sort_order`   int            NOT NULL DEFAULT 0 COMMENT '排序',
    `status`       tinyint        NOT NULL DEFAULT 1 COMMENT '状态（1-启用，0-停用）',
    `create_time`  bigint                  DEFAULT NULL COMMENT '数据创建时间',
    `update_time`  bigint                  DEFAULT NULL COMMENT '数据修改时间',
    `creator`      varchar(50)             DEFAULT '' COMMENT '数据创建者',
    `updater`      varchar(50)             DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_vip_level` (`level`),
    KEY            `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB COMMENT='VIP等级运营配置表';

CREATE TABLE `vip_upgrade_order`
(
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_no`       varchar(64)    NOT NULL COMMENT '升级订单号',
    `user_id`        bigint         NOT NULL COMMENT '全局唯一用户ID',
    `from_level`     int            NOT NULL COMMENT '升级前等级',
    `to_level`       int            NOT NULL COMMENT '目标等级',
    `currency`       varchar(16)    NOT NULL DEFAULT 'USDT' COMMENT '扣款币种',
    `amount`         decimal(24, 6) NOT NULL COMMENT '扣款金额',
    `status`         varchar(20)    NOT NULL COMMENT '状态：PENDING/SUCCESS',
    `wallet_flow_no` varchar(64)             DEFAULT NULL COMMENT '钱包扣款流水号',
    `finished_at`    bigint                  DEFAULT NULL COMMENT '完成时间',
    `remark`         varchar(255)            DEFAULT '' COMMENT '备注',
    `create_time`    bigint                  DEFAULT NULL COMMENT '数据创建时间',
    `update_time`    bigint                  DEFAULT NULL COMMENT '数据修改时间',
    `creator`        varchar(50)             DEFAULT '' COMMENT '数据创建者',
    `updater`        varchar(50)             DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    UNIQUE KEY `uk_user_level_status` (`user_id`, `to_level`, `status`),
    KEY              `idx_user_time` (`user_id`, `create_time`),
    KEY              `idx_status_time` (`status`, `create_time`)
) ENGINE=InnoDB COMMENT='VIP升级订单表';

CREATE TABLE `invite_commission_record`
(
    `id`                bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `record_no`         varchar(64)    NOT NULL COMMENT '返佣记录号',
    `inviter_user_id`   bigint         NOT NULL COMMENT '获得返佣的邀请人user_id',
    `source_user_id`    bigint         NOT NULL COMMENT '触发返佣的下级user_id',
    `source_order_no`   varchar(64)    NOT NULL COMMENT '触发业务订单号',
    `business_type`     varchar(32)    NOT NULL COMMENT '触发业务类型',
    `currency`          varchar(16)    NOT NULL DEFAULT 'USDT' COMMENT '返佣币种',
    `source_amount`     decimal(24, 6) NOT NULL COMMENT '触发业务金额快照',
    `commission_rate`   decimal(10, 4) NOT NULL COMMENT '返佣比例快照',
    `commission_amount` decimal(24, 6) NOT NULL COMMENT '返佣金额',
    `status`            varchar(20)    NOT NULL COMMENT '状态：PENDING/SETTLED/SETTLE_FAILED',
    `wallet_flow_no`    varchar(64)             DEFAULT NULL COMMENT '钱包入账流水号',
    `fail_reason`       varchar(500)            DEFAULT NULL COMMENT '结算失败原因',
    `settled_at`        bigint                  DEFAULT NULL COMMENT '结算时间',
    `create_time`       bigint                  DEFAULT NULL COMMENT '数据创建时间',
    `update_time`       bigint                  DEFAULT NULL COMMENT '数据修改时间',
    `creator`           varchar(50)             DEFAULT '' COMMENT '数据创建者',
    `updater`           varchar(50)             DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_record_no` (`record_no`),
    UNIQUE KEY `uk_biz_order` (`business_type`, `source_order_no`),
    KEY                 `idx_inviter_time` (`inviter_user_id`, `create_time`),
    KEY                 `idx_source_user` (`source_user_id`),
    KEY                 `idx_status_time` (`status`, `create_time`)
) ENGINE=InnoDB COMMENT='邀请返佣记录表';

INSERT INTO `vip_level_config`(`level`, `level_name`, `price`, `rebate_rate`, `daily_tasks`, `benefits`, `sort_order`, `status`, `creator`, `updater`, `create_time`, `update_time`)
VALUES (0, 'VIP0', 0.000000, 0.0000, 0, '基础会员', 0, 1, '', '', 1782880000000, NULL),
       (1, 'VIP1', 100.000000, 0.0100, 5, '每日任务数 5\n基础返佣比例 1%', 1, 1, '', '', 1782880000000, NULL),
       (2, 'VIP2', 300.000000, 0.0200, 10, '每日任务数 10\n基础返佣比例 2%', 2, 1, '', '', 1782880000000, NULL),
       (3, 'VIP3', 800.000000, 0.0300, 20, '每日任务数 20\n基础返佣比例 3%', 3, 1, '', '', 1782880000000, NULL)
ON DUPLICATE KEY UPDATE `level_name` = VALUES(`level_name`),
                        `price` = VALUES(`price`),
                        `rebate_rate` = VALUES(`rebate_rate`),
                        `daily_tasks` = VALUES(`daily_tasks`),
                        `benefits` = VALUES(`benefits`),
                        `sort_order` = VALUES(`sort_order`),
                        `status` = VALUES(`status`),
                        `update_time` = VALUES(`create_time`);
