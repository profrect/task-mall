-- =============================================================
-- mall-promotion 抽奖闭环初始化
-- 1) promotion_prize 定义奖品配置。
-- 2) promotion_lottery_activity 定义抽奖活动。
-- 3) promotion_lottery_prize 定义活动奖池、权重与限制。
-- 4) promotion_lottery_record 承载用户抽奖事实和钱包入账流水号。
-- 5) 种子数据只写配置，不伪造用户中奖记录、收益或钱包流水。
-- =============================================================

CREATE TABLE IF NOT EXISTS `promotion_prize` (
    `id`           bigint         NOT NULL AUTO_INCREMENT,
    `prize_code`   varchar(64)    NOT NULL COMMENT '奖品编码',
    `prize_name`   varchar(100)   NOT NULL COMMENT '奖品名称',
    `prize_type`   varchar(32)    NOT NULL COMMENT 'CASH/POINT/COUPON/PHYSICAL',
    `currency`     varchar(16)    NOT NULL DEFAULT 'USDT' COMMENT '现金奖币种',
    `amount`       decimal(24, 6) NOT NULL DEFAULT 0.000000 COMMENT '现金奖金额',
    `stock_total`  int            NOT NULL DEFAULT 0 COMMENT '总库存，0表示不限量',
    `stock_used`   int            NOT NULL DEFAULT 0 COMMENT '已使用库存',
    `sort_order`   int            NOT NULL DEFAULT 0 COMMENT '排序',
    `status`       tinyint        NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    `remark`       varchar(500)            DEFAULT NULL COMMENT '备注',
    `creator`      varchar(64)             DEFAULT NULL,
    `updater`      varchar(64)             DEFAULT NULL,
    `create_time`  bigint                  DEFAULT NULL,
    `update_time`  bigint                  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prize_code` (`prize_code`),
    KEY `idx_status_type` (`status`, `prize_type`)
) ENGINE=InnoDB COMMENT='促销奖品配置';

CREATE TABLE IF NOT EXISTS `promotion_lottery_activity` (
    `id`            bigint       NOT NULL AUTO_INCREMENT,
    `activity_code` varchar(64)  NOT NULL COMMENT '活动编码',
    `title`         varchar(100) NOT NULL COMMENT '活动标题',
    `description`   varchar(1000)         DEFAULT NULL COMMENT '活动说明',
    `daily_limit`   int          NOT NULL DEFAULT 1 COMMENT '每人每日次数，0表示不限制',
    `start_at`      bigint                DEFAULT NULL COMMENT '开始时间毫秒',
    `end_at`        bigint                DEFAULT NULL COMMENT '结束时间毫秒',
    `sort_order`    int          NOT NULL DEFAULT 0 COMMENT '排序',
    `status`        tinyint      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    `creator`       varchar(64)           DEFAULT NULL,
    `updater`       varchar(64)           DEFAULT NULL,
    `create_time`   bigint                DEFAULT NULL,
    `update_time`   bigint                DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_code` (`activity_code`),
    KEY `idx_status_time` (`status`, `start_at`, `end_at`)
) ENGINE=InnoDB COMMENT='抽奖活动配置';

CREATE TABLE IF NOT EXISTS `promotion_lottery_prize` (
    `id`           bigint  NOT NULL AUTO_INCREMENT,
    `activity_id`  bigint  NOT NULL COMMENT '活动ID',
    `prize_id`     bigint  NOT NULL COMMENT '奖品ID',
    `weight`       int     NOT NULL DEFAULT 1 COMMENT '抽中权重',
    `daily_limit`  int     NOT NULL DEFAULT 0 COMMENT '该奖品每日发放限制，0表示不限制',
    `sort_order`   int     NOT NULL DEFAULT 0 COMMENT '排序',
    `status`       tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    `creator`      varchar(64)      DEFAULT NULL,
    `updater`      varchar(64)      DEFAULT NULL,
    `create_time`  bigint           DEFAULT NULL,
    `update_time`  bigint           DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_prize` (`activity_id`, `prize_id`),
    KEY `idx_activity_status` (`activity_id`, `status`),
    KEY `idx_prize_id` (`prize_id`)
) ENGINE=InnoDB COMMENT='抽奖活动奖池配置';

CREATE TABLE IF NOT EXISTS `promotion_lottery_record` (
    `id`             bigint         NOT NULL AUTO_INCREMENT,
    `record_no`      varchar(64)    NOT NULL COMMENT '抽奖记录号',
    `user_id`        bigint         NOT NULL COMMENT '用户ID',
    `activity_id`    bigint         NOT NULL COMMENT '活动ID',
    `activity_title` varchar(100)   NOT NULL COMMENT '活动标题快照',
    `prize_id`       bigint         NOT NULL COMMENT '奖品ID',
    `prize_code`     varchar(64)    NOT NULL COMMENT '奖品编码快照',
    `prize_name`     varchar(100)   NOT NULL COMMENT '奖品名称快照',
    `prize_type`     varchar(32)    NOT NULL COMMENT '奖品类型快照',
    `currency`       varchar(16)    NOT NULL DEFAULT 'USDT' COMMENT '币种快照',
    `amount`         decimal(24, 6) NOT NULL DEFAULT 0.000000 COMMENT '金额快照',
    `status`         varchar(32)    NOT NULL COMMENT 'DRAWN/SETTLED/SETTLE_FAILED',
    `wallet_flow_no` varchar(64)             DEFAULT NULL COMMENT '现金奖钱包流水号',
    `fail_reason`    varchar(500)            DEFAULT NULL COMMENT '结算失败原因',
    `drawn_at`       bigint         NOT NULL COMMENT '抽奖时间毫秒',
    `settled_at`     bigint                  DEFAULT NULL COMMENT '结算时间毫秒',
    `creator`        varchar(64)             DEFAULT NULL,
    `updater`        varchar(64)             DEFAULT NULL,
    `create_time`    bigint                  DEFAULT NULL,
    `update_time`    bigint                  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_record_no` (`record_no`),
    KEY `idx_user_activity_day` (`user_id`, `activity_id`, `drawn_at`),
    KEY `idx_prize_day` (`prize_id`, `drawn_at`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='用户抽奖记录';

INSERT INTO `promotion_prize`(`id`, `prize_code`, `prize_name`, `prize_type`, `currency`, `amount`, `stock_total`, `stock_used`, `sort_order`, `status`, `remark`, `creator`, `updater`, `create_time`, `update_time`) VALUES
(1, 'LOTTERY_CASH_001', '现金奖励 0.100000 USDT', 'CASH', 'USDT', 0.100000, 0, 0, 1, 1, '现金奖中奖后通过钱包结算 Provider 入账', '', '', 1782890000000, NULL),
(2, 'LOTTERY_CASH_002', '现金奖励 0.500000 USDT', 'CASH', 'USDT', 0.500000, 0, 0, 2, 1, '现金奖中奖后通过钱包结算 Provider 入账', '', '', 1782890000000, NULL),
(3, 'LOTTERY_THANKS', '谢谢参与', 'POINT', 'USDT', 0.000000, 0, 0, 99, 1, '非现金奖，只记录中奖事实，不触发钱包入账', '', '', 1782890000000, NULL)
ON DUPLICATE KEY UPDATE
    `prize_name` = VALUES(`prize_name`),
    `prize_type` = VALUES(`prize_type`),
    `currency` = VALUES(`currency`),
    `amount` = VALUES(`amount`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`);

INSERT INTO `promotion_lottery_activity`(`id`, `activity_code`, `title`, `description`, `daily_limit`, `start_at`, `end_at`, `sort_order`, `status`, `creator`, `updater`, `create_time`, `update_time`) VALUES
(1, 'DAILY_LOTTERY', '每日抽奖', '用户每日可参与的基础抽奖活动，中奖现金奖后写入可追踪抽奖记录并调用钱包结算入账。', 1, NULL, NULL, 1, 1, '', '', 1782890000000, NULL)
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `description` = VALUES(`description`),
    `daily_limit` = VALUES(`daily_limit`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`);

INSERT INTO `promotion_lottery_prize`(`id`, `activity_id`, `prize_id`, `weight`, `daily_limit`, `sort_order`, `status`, `creator`, `updater`, `create_time`, `update_time`) VALUES
(1, 1, 1, 20, 0, 1, 1, '', '', 1782890000000, NULL),
(2, 1, 2, 5, 0, 2, 1, '', '', 1782890000000, NULL),
(3, 1, 3, 75, 0, 99, 1, '', '', 1782890000000, NULL)
ON DUPLICATE KEY UPDATE
    `weight` = VALUES(`weight`),
    `daily_limit` = VALUES(`daily_limit`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`);