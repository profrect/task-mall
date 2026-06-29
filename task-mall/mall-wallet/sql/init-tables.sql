CREATE TABLE `wallet_account`
(
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`        bigint         NOT NULL COMMENT '全局唯一用户ID',
    `total_balance`  decimal(16, 3) NOT NULL DEFAULT 0 COMMENT '总余额',
    `avail_balance`  decimal(16, 3) NOT NULL DEFAULT 0 COMMENT '可用余额',
    `frozen_balance` decimal(16, 3) NOT NULL DEFAULT 0 COMMENT '冻结余额',
    `currency`       varchar(6)     NOT NULL DEFAULT 'USDT' COMMENT '钱包币种',
    `create_time`    bigint                  DEFAULT NULL COMMENT '数据创建时间',
    `update_time`    bigint                  DEFAULT NULL COMMENT '数据修改时间',
    `creator`        varchar(50)             DEFAULT '' COMMENT '数据创建者',
    `updater`        varchar(50)             DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    KEY              `uk_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='钱包账户主表';

CREATE TABLE `wallet_flow_detail`
(
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `flow_no`        varchar(50)    NOT NULL COMMENT '流水号',
    `user_id`        bigint         NOT NULL COMMENT '全局唯一用户ID',
    `wallet_id`      bigint         NOT NULL COMMENT '钱包账户记录id',
    `biz_type`       varchar(50)    NOT NULL COMMENT '业务类型:RECHARGE/WITHDRAW/TRANSFER/PAYMENT',
    `biz_id`         varchar(50)    NOT NULL COMMENT '业务单号: 如充值订单号、支付订单号等',
    `direction`      varchar(6)     NOT NULL COMMENT '资金方向(IN/OUT)',
    `change_amt`     decimal(16, 3) NOT NULL DEFAULT 0.000 COMMENT '变动金额',
    `balance_before` decimal(16, 3) NOT NULL DEFAULT 0.000 COMMENT '变动前可用余额快照',
    `balance_after`  decimal(16, 3) NOT NULL DEFAULT 0.000 COMMENT '变动后可用余额快照',
    `desc`           varchar(200) NULL COMMENT '描述',
    `create_time`    bigint                  DEFAULT NULL COMMENT '数据创建时间',
    `update_time`    bigint                  DEFAULT NULL COMMENT '数据修改时间',
    `creator`        varchar(50)             DEFAULT '' COMMENT '数据创建者',
    `updater`        varchar(50)             DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_no` (`flow_no`),
    KEY              `idx_user_id` (`user_id`),
    KEY              `idx_user_id` (`wallet_id`)
) ENGINE=InnoDB COMMENT='钱包流水明细';



CREATE TABLE `wallet_recharge_order`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     bigint NOT NULL COMMENT '全局唯一用户ID',

    `create_time` bigint      DEFAULT NULL COMMENT '数据创建时间',
    `update_time` bigint      DEFAULT NULL COMMENT '数据修改时间',
    `creator`     varchar(50) DEFAULT '' COMMENT '数据创建者',
    `updater`     varchar(50) DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    KEY           `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='充值订单';

CREATE TABLE `wallet_withdraw_order`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     bigint NOT NULL COMMENT '全局唯一用户ID',

    `create_time` bigint      DEFAULT NULL COMMENT '数据创建时间',
    `update_time` bigint      DEFAULT NULL COMMENT '数据修改时间',
    `creator`     varchar(50) DEFAULT '' COMMENT '数据创建者',
    `updater`     varchar(50) DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    KEY           `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='提现订单';

CREATE TABLE `wallet_transfer_order`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     bigint NOT NULL COMMENT '全局唯一用户ID',

    `create_time` bigint      DEFAULT NULL COMMENT '数据创建时间',
    `update_time` bigint      DEFAULT NULL COMMENT '数据修改时间',
    `creator`     varchar(50) DEFAULT '' COMMENT '数据创建者',
    `updater`     varchar(50) DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    KEY           `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='转账订单';

CREATE TABLE `wallet_payment_order`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     bigint NOT NULL COMMENT '全局唯一用户ID',

    `create_time` bigint      DEFAULT NULL COMMENT '数据创建时间',
    `update_time` bigint      DEFAULT NULL COMMENT '数据修改时间',
    `creator`     varchar(50) DEFAULT '' COMMENT '数据创建者',
    `updater`     varchar(50) DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    KEY           `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='支付订单';

CREATE TABLE `wallet_order_audit`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     bigint NOT NULL COMMENT '全局唯一用户ID',

    `create_time` bigint      DEFAULT NULL COMMENT '数据创建时间',
    `update_time` bigint      DEFAULT NULL COMMENT '数据修改时间',
    `creator`     varchar(50) DEFAULT '' COMMENT '数据创建者',
    `updater`     varchar(50) DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    KEY           `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='订单审计';

CREATE TABLE `wallet_payment_channel`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     bigint NOT NULL COMMENT '全局唯一用户ID',

    `create_time` bigint      DEFAULT NULL COMMENT '数据创建时间',
    `update_time` bigint      DEFAULT NULL COMMENT '数据修改时间',
    `creator`     varchar(50) DEFAULT '' COMMENT '数据创建者',
    `updater`     varchar(50) DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    KEY           `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='支付通道信息';

CREATE TABLE `wallet_payment_pin`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',

    `create_time` bigint      DEFAULT NULL COMMENT '数据创建时间',
    `update_time` bigint      DEFAULT NULL COMMENT '数据修改时间',
    `creator`     varchar(50) DEFAULT '' COMMENT '数据创建者',
    `updater`     varchar(50) DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='用户支付PIN码/资金密码';

CREATE TABLE `wallet_freeze_record`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',

    `create_time` bigint      DEFAULT NULL COMMENT '数据创建时间',
    `update_time` bigint      DEFAULT NULL COMMENT '数据修改时间',
    `creator`     varchar(50) DEFAULT '' COMMENT '数据创建者',
    `updater`     varchar(50) DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='资金冻结信息';
