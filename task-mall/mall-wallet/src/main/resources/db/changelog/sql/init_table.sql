-- liquibase formatted sql
-- changeset GM:wallet_init_tables_20260629001

-- 注意：本项目 Liquibase 默认 spring.liquibase.enabled=false，建表通过手工执行 SQL 完成。
-- mall-wallet 的权威 schema 源为：mall-wallet/sql/init-tables.sql（连同 sql/init-database.sql 建库）。
-- 为避免“双份 schema 漂移”，此处不再重复维护建表语句；
-- 若将来启用 Liquibase，请将权威 DDL 迁移到此 changeset 并删除手工 SQL，保持单一事实来源。
-- 当前新增表请同步维护 mall-wallet/sql/init-tables.sql：wallet_transfer_order、payment_order。

-- changeset GM:payment_order_20260701002
CREATE TABLE IF NOT EXISTS `payment_order`
(
    `id`                bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_no`          varchar(64)    NOT NULL COMMENT '支付审计订单号',
    `user_id`           bigint         NOT NULL COMMENT '用户ID',
    `business_type`     varchar(32)    NOT NULL COMMENT '业务类型: RECHARGE/VIP_UPGRADE/OTHER',
    `business_order_no` varchar(64)             DEFAULT NULL COMMENT '关联业务订单号',
    `channel_code`      varchar(32)    NOT NULL COMMENT '支付通道: CHAIN/TRON/ETH/MANUAL/...',
    `channel_order_no`  varchar(128)            DEFAULT NULL COMMENT '通道订单号',
    `currency`          varchar(16)    NOT NULL DEFAULT 'USDT' COMMENT '币种',
    `amount`            decimal(36, 6) NOT NULL DEFAULT 0 COMMENT '支付金额',
    `status`            varchar(16)    NOT NULL DEFAULT 'CREATED' COMMENT '状态: CREATED/PENDING/PAID/CLOSED/FAILED',
    `pay_address`       varchar(128)            DEFAULT NULL COMMENT '收款地址/支付目标',
    `payer_address`     varchar(128)            DEFAULT NULL COMMENT '付款地址',
    `tx_hash`           varchar(128)            DEFAULT NULL COMMENT '链上交易哈希/外部凭证号',
    `audit_remark`      varchar(512)            DEFAULT NULL COMMENT '审计备注',
    `paid_at`           bigint                  DEFAULT NULL COMMENT '支付完成时间（UTC毫秒）',
    `expired_at`        bigint                  DEFAULT NULL COMMENT '过期时间（UTC毫秒）',
    `create_time`       bigint                  DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time`       bigint                  DEFAULT NULL COMMENT '数据修改时间（UTC毫秒）',
    `creator`           varchar(50)             DEFAULT '' COMMENT '数据创建者',
    `updater`           varchar(50)             DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user` (`user_id`),
    KEY `idx_business` (`business_type`, `business_order_no`),
    KEY `idx_channel` (`channel_code`, `channel_order_no`),
    KEY `idx_status` (`status`),
    KEY `idx_tx_hash` (`tx_hash`)
) ENGINE = InnoDB COMMENT ='支付订单审计';

-- changeset GM:wallet_transfer_order_20260701001
CREATE TABLE IF NOT EXISTS `wallet_transfer_order`
(
    `id`           bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_no`     varchar(64)    NOT NULL COMMENT '转账订单号(内部唯一，账务幂等键)',
    `from_user_id` bigint         NOT NULL COMMENT '转出用户ID',
    `to_user_id`   bigint         NOT NULL COMMENT '转入用户ID',
    `coin`         varchar(16)    NOT NULL DEFAULT 'USDT' COMMENT '币种',
    `amount`       decimal(36, 6) NOT NULL COMMENT '转账金额(恒为正)',
    `status`       varchar(16)    NOT NULL DEFAULT 'SUCCESS' COMMENT '状态: SUCCESS/FAILED',
    `remark`       varchar(200)            DEFAULT NULL COMMENT '用户备注',
    `finished_at`  bigint                  DEFAULT NULL COMMENT '完成时间（UTC毫秒）',
    `create_time`  bigint                  DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time`  bigint                  DEFAULT NULL COMMENT '数据修改时间（UTC毫秒）',
    `creator`      varchar(50)             DEFAULT '' COMMENT '数据创建者',
    `updater`      varchar(50)             DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_from_user` (`from_user_id`),
    KEY `idx_to_user` (`to_user_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB COMMENT ='站内转账订单';