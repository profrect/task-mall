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

-- =============================================================
-- 优惠券与签到：业务事实留在 promotion，签到奖励通过 wallet settlement 入账
-- =============================================================

CREATE TABLE IF NOT EXISTS `promotion_coupon_template` (
    `id`               bigint         NOT NULL AUTO_INCREMENT,
    `coupon_code`      varchar(64)    NOT NULL COMMENT '优惠券编码',
    `title`            varchar(100)   NOT NULL COMMENT '优惠券标题',
    `description`      varchar(1000)           DEFAULT NULL COMMENT '说明',
    `coupon_type`      varchar(32)    NOT NULL DEFAULT 'CASH_OFF' COMMENT 'CASH_OFF/PERCENT 等扩展类型',
    `currency`         varchar(16)    NOT NULL DEFAULT 'USDT',
    `discount_amount`  decimal(24, 6) NOT NULL DEFAULT 0.000000 COMMENT '抵扣金额',
    `min_order_amount` decimal(24, 6) NOT NULL DEFAULT 0.000000 COMMENT '最低使用金额',
    `total_stock`      int            NOT NULL DEFAULT 0 COMMENT '总库存，0表示不限量',
    `claimed_stock`    int            NOT NULL DEFAULT 0 COMMENT '已领取库存',
    `per_user_limit`   int            NOT NULL DEFAULT 1 COMMENT '每人领取上限',
    `valid_days`       int            NOT NULL DEFAULT 30 COMMENT '领取后有效天数',
    `start_at`         bigint                  DEFAULT NULL,
    `end_at`           bigint                  DEFAULT NULL,
    `sort_order`       int            NOT NULL DEFAULT 0,
    `status`           tinyint        NOT NULL DEFAULT 1,
    `creator`          varchar(64)             DEFAULT NULL,
    `updater`          varchar(64)             DEFAULT NULL,
    `create_time`      bigint                  DEFAULT NULL,
    `update_time`      bigint                  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_coupon_code` (`coupon_code`),
    KEY `idx_status_time` (`status`, `start_at`, `end_at`)
) ENGINE=InnoDB COMMENT='优惠券模板';

CREATE TABLE IF NOT EXISTS `promotion_coupon_record` (
    `id`               bigint         NOT NULL AUTO_INCREMENT,
    `record_no`        varchar(96)    NOT NULL COMMENT '领取记录号',
    `user_id`          bigint         NOT NULL,
    `template_id`      bigint         NOT NULL,
    `coupon_code`      varchar(64)    NOT NULL,
    `title`            varchar(100)   NOT NULL,
    `coupon_type`      varchar(32)    NOT NULL,
    `currency`         varchar(16)    NOT NULL DEFAULT 'USDT',
    `discount_amount`  decimal(24, 6) NOT NULL DEFAULT 0.000000,
    `min_order_amount` decimal(24, 6) NOT NULL DEFAULT 0.000000,
    `status`           varchar(32)    NOT NULL COMMENT 'CLAIMED/LOCKED/USED/EXPIRED',
    `locked_biz_type`  varchar(64)             DEFAULT NULL,
    `locked_biz_id`    varchar(96)             DEFAULT NULL,
    `used_biz_type`    varchar(64)             DEFAULT NULL,
    `used_biz_id`      varchar(96)             DEFAULT NULL,
    `valid_from`       bigint         NOT NULL,
    `valid_to`         bigint         NOT NULL,
    `claimed_at`       bigint         NOT NULL,
    `locked_at`        bigint                  DEFAULT NULL,
    `used_at`          bigint                  DEFAULT NULL,
    `expired_at`       bigint                  DEFAULT NULL,
    `creator`          varchar(64)             DEFAULT NULL,
    `updater`          varchar(64)             DEFAULT NULL,
    `create_time`      bigint                  DEFAULT NULL,
    `update_time`      bigint                  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_record_no` (`record_no`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_template_user` (`template_id`, `user_id`),
    KEY `idx_valid_to` (`valid_to`)
) ENGINE=InnoDB COMMENT='用户优惠券记录';

CREATE TABLE IF NOT EXISTS `promotion_checkin_rule` (
    `id`                        bigint         NOT NULL AUTO_INCREMENT,
    `rule_code`                 varchar(64)    NOT NULL,
    `title`                     varchar(100)   NOT NULL,
    `required_consecutive_days` int            NOT NULL DEFAULT 1,
    `currency`                  varchar(16)    NOT NULL DEFAULT 'USDT',
    `reward_amount`             decimal(24, 6) NOT NULL DEFAULT 0.000000,
    `sort_order`                int            NOT NULL DEFAULT 0,
    `status`                    tinyint        NOT NULL DEFAULT 1,
    `creator`                   varchar(64)             DEFAULT NULL,
    `updater`                   varchar(64)             DEFAULT NULL,
    `create_time`               bigint                  DEFAULT NULL,
    `update_time`               bigint                  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_code` (`rule_code`),
    KEY `idx_status_days` (`status`, `required_consecutive_days`)
) ENGINE=InnoDB COMMENT='签到奖励规则';

CREATE TABLE IF NOT EXISTS `promotion_checkin_record` (
    `id`               bigint         NOT NULL AUTO_INCREMENT,
    `record_no`        varchar(96)    NOT NULL,
    `user_id`          bigint         NOT NULL,
    `checkin_date`     int            NOT NULL COMMENT 'yyyyMMdd',
    `consecutive_days` int            NOT NULL DEFAULT 1,
    `currency`         varchar(16)    NOT NULL DEFAULT 'USDT',
    `reward_amount`    decimal(24, 6) NOT NULL DEFAULT 0.000000,
    `status`           varchar(32)    NOT NULL COMMENT 'SETTLED/SETTLE_FAILED',
    `wallet_flow_no`   varchar(64)             DEFAULT NULL,
    `fail_reason`      varchar(500)            DEFAULT NULL,
    `checked_at`       bigint         NOT NULL,
    `settled_at`       bigint                  DEFAULT NULL,
    `creator`          varchar(64)             DEFAULT NULL,
    `updater`          varchar(64)             DEFAULT NULL,
    `create_time`      bigint                  DEFAULT NULL,
    `update_time`      bigint                  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `checkin_date`),
    UNIQUE KEY `uk_record_no` (`record_no`),
    KEY `idx_user_date` (`user_id`, `checkin_date`)
) ENGINE=InnoDB COMMENT='用户签到记录';

INSERT INTO `promotion_coupon_template`(`id`, `coupon_code`, `title`, `description`, `coupon_type`, `currency`, `discount_amount`, `min_order_amount`, `total_stock`, `claimed_stock`, `per_user_limit`, `valid_days`, `start_at`, `end_at`, `sort_order`, `status`, `creator`, `updater`, `create_time`, `update_time`) VALUES
(1, 'WELCOME_COUPON_5', '新人抵扣券', '领取后 30 天内有效。当前只承载领取和记录状态，具体订单核销待订单域接入。', 'CASH_OFF', 'USDT', 5.000000, 50.000000, 0, 0, 1, 30, NULL, NULL, 1, 1, '', '', 1782890000000, NULL),
(2, 'VIP_COUPON_10', 'VIP 专属抵扣券', '演示券模板：状态机支持领取、锁定、使用、过期。', 'CASH_OFF', 'USDT', 10.000000, 100.000000, 0, 0, 1, 30, NULL, NULL, 2, 1, '', '', 1782890000000, NULL)
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `description` = VALUES(`description`),
    `discount_amount` = VALUES(`discount_amount`),
    `min_order_amount` = VALUES(`min_order_amount`),
    `per_user_limit` = VALUES(`per_user_limit`),
    `valid_days` = VALUES(`valid_days`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`);

INSERT INTO `promotion_checkin_rule`(`id`, `rule_code`, `title`, `required_consecutive_days`, `currency`, `reward_amount`, `sort_order`, `status`, `creator`, `updater`, `create_time`, `update_time`) VALUES
(1, 'CHECKIN_DAY_1', '每日签到奖励', 1, 'USDT', 0.100000, 1, 1, '', '', 1782890000000, NULL),
(2, 'CHECKIN_DAY_3', '连续 3 天奖励', 3, 'USDT', 0.300000, 3, 1, '', '', 1782890000000, NULL),
(3, 'CHECKIN_DAY_7', '连续 7 天奖励', 7, 'USDT', 1.000000, 7, 1, '', '', 1782890000000, NULL)
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `required_consecutive_days` = VALUES(`required_consecutive_days`),
    `currency` = VALUES(`currency`),
    `reward_amount` = VALUES(`reward_amount`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`);