-- =============================================================
-- mall-mission 任务中心 schema（手工执行；本项目 Liquibase 默认 enabled:false）
--
-- 边界：
-- 1) mission_goods 只承载任务中心商品/投资项目展示口径，不建立独立投资账本。
-- 2) mission_task 定义可领取任务。
-- 3) mission_user_task 承载领取、提交、审核状态机。
-- 4) mission_reward_settlement 承载任务审核通过后的钱包结算对账。
-- 5) 种子数据只写任务/项目配置，不伪造用户完成记录或收益。
-- =============================================================

CREATE TABLE `mission_goods`
(
    `id`           bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `goods_type`   varchar(32)    NOT NULL COMMENT '类型：TASK_PRODUCT/INVEST_PROJECT',
    `goods_code`   varchar(64)    NOT NULL COMMENT '展示项目编码',
    `title`        varchar(100)   NOT NULL COMMENT '标题',
    `description`  varchar(1000)           DEFAULT NULL COMMENT '说明',
    `currency`     varchar(16)    NOT NULL DEFAULT 'USDT' COMMENT '展示币种',
    `min_amount`   decimal(24, 6) NOT NULL DEFAULT 0.000000 COMMENT '最小展示金额',
    `max_amount`   decimal(24, 6) NOT NULL DEFAULT 0.000000 COMMENT '最大展示金额',
    `display_rate` decimal(10, 4) NOT NULL DEFAULT 0.0000 COMMENT '展示收益率/比例，仅展示',
    `cycle_days`   int            NOT NULL DEFAULT 0 COMMENT '展示周期天数',
    `risk_level`   varchar(20)    NOT NULL DEFAULT 'LOW' COMMENT '展示风险等级',
    `sort_order`   int            NOT NULL DEFAULT 0 COMMENT '排序',
    `status`       tinyint        NOT NULL DEFAULT 1 COMMENT '状态（1-启用，0-停用）',
    `create_time`  bigint                  DEFAULT NULL COMMENT '数据创建时间',
    `update_time`  bigint                  DEFAULT NULL COMMENT '数据修改时间',
    `creator`      varchar(50)             DEFAULT '' COMMENT '数据创建者',
    `updater`      varchar(50)             DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_goods_code` (`goods_code`),
    KEY            `idx_type_status_sort` (`goods_type`, `status`, `sort_order`)
) ENGINE=InnoDB COMMENT='任务中心商品/投资项目展示配置';

CREATE TABLE `mission_task`
(
    `id`                 bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `task_code`          varchar(64)    NOT NULL COMMENT '任务编码',
    `title`              varchar(100)   NOT NULL COMMENT '任务标题',
    `description`        varchar(1000)           DEFAULT NULL COMMENT '任务说明',
    `task_type`          varchar(32)    NOT NULL DEFAULT 'GENERAL' COMMENT '任务类型',
    `currency`           varchar(16)    NOT NULL DEFAULT 'USDT' COMMENT '奖励币种',
    `reward_amount`      decimal(24, 6) NOT NULL COMMENT '审核通过奖励金额',
    `required_vip_level` int            NOT NULL DEFAULT 0 COMMENT '领取所需VIP等级（展示/后续校验扩展）',
    `daily_limit`        int            NOT NULL DEFAULT 0 COMMENT '每日限制，0表示不限制（后续计划扩展）',
    `start_at`           bigint                  DEFAULT NULL COMMENT '开始时间',
    `end_at`             bigint                  DEFAULT NULL COMMENT '结束时间',
    `sort_order`         int            NOT NULL DEFAULT 0 COMMENT '排序',
    `status`             tinyint        NOT NULL DEFAULT 1 COMMENT '状态（1-启用，0-停用）',
    `create_time`        bigint                  DEFAULT NULL COMMENT '数据创建时间',
    `update_time`        bigint                  DEFAULT NULL COMMENT '数据修改时间',
    `creator`            varchar(50)             DEFAULT '' COMMENT '数据创建者',
    `updater`            varchar(50)             DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_code` (`task_code`),
    KEY                  `idx_status_sort` (`status`, `sort_order`),
    KEY                  `idx_type_status` (`task_type`, `status`)
) ENGINE=InnoDB COMMENT='任务配置';

CREATE TABLE `mission_user_task`
(
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`        bigint         NOT NULL COMMENT '全局唯一用户ID',
    `task_id`        bigint         NOT NULL COMMENT '任务ID',
    `task_code`      varchar(64)    NOT NULL COMMENT '任务编码快照',
    `task_title`     varchar(100)   NOT NULL COMMENT '任务标题快照',
    `task_type`      varchar(32)    NOT NULL COMMENT '任务类型快照',
    `currency`       varchar(16)    NOT NULL DEFAULT 'USDT' COMMENT '奖励币种快照',
    `reward_amount`  decimal(24, 6) NOT NULL COMMENT '奖励金额快照',
    `status`         varchar(20)    NOT NULL COMMENT 'CLAIMED/SUBMITTED/APPROVED/REJECTED/CANCELLED/EXPIRED',
    `submit_content` varchar(1000)           DEFAULT NULL COMMENT '用户提交证明',
    `review_remark`  varchar(500)            DEFAULT NULL COMMENT '审核备注',
    `wallet_flow_no` varchar(64)             DEFAULT NULL COMMENT '审核通过后的钱包流水号',
    `claimed_at`     bigint                  DEFAULT NULL COMMENT '领取时间',
    `submitted_at`   bigint                  DEFAULT NULL COMMENT '提交时间',
    `reviewed_at`    bigint                  DEFAULT NULL COMMENT '审核时间',
    `finished_at`    bigint                  DEFAULT NULL COMMENT '终态时间',
    `create_time`    bigint                  DEFAULT NULL COMMENT '数据创建时间',
    `update_time`    bigint                  DEFAULT NULL COMMENT '数据修改时间',
    `creator`        varchar(50)             DEFAULT '' COMMENT '数据创建者',
    `updater`        varchar(50)             DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_task_status` (`user_id`, `task_id`, `status`),
    KEY              `idx_user_status` (`user_id`, `status`),
    KEY              `idx_status_time` (`status`, `create_time`),
    KEY              `idx_task` (`task_id`)
) ENGINE=InnoDB COMMENT='用户任务记录';

CREATE TABLE `mission_reward_settlement`
(
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_task_id`   bigint         NOT NULL COMMENT '用户任务记录ID',
    `user_id`        bigint         NOT NULL COMMENT '全局唯一用户ID',
    `task_id`        bigint         NOT NULL COMMENT '任务ID',
    `currency`       varchar(16)    NOT NULL DEFAULT 'USDT' COMMENT '奖励币种',
    `amount`         decimal(24, 6) NOT NULL COMMENT '奖励金额',
    `biz_type`       varchar(32)    NOT NULL DEFAULT 'TASK_REWARD' COMMENT '钱包业务类型',
    `biz_id`         varchar(128)   NOT NULL COMMENT '钱包结算幂等键',
    `status`         varchar(20)    NOT NULL COMMENT 'PENDING/SETTLED/FAILED',
    `wallet_flow_no` varchar(64)             DEFAULT NULL COMMENT '钱包流水号',
    `fail_reason`    varchar(500)            DEFAULT NULL COMMENT '失败原因',
    `settled_at`     bigint                  DEFAULT NULL COMMENT '结算完成时间',
    `create_time`    bigint                  DEFAULT NULL COMMENT '数据创建时间',
    `update_time`    bigint                  DEFAULT NULL COMMENT '数据修改时间',
    `creator`        varchar(50)             DEFAULT '' COMMENT '数据创建者',
    `updater`        varchar(50)             DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_task` (`user_task_id`),
    UNIQUE KEY `uk_wallet_biz` (`biz_type`, `biz_id`),
    KEY              `idx_user_status` (`user_id`, `status`),
    KEY              `idx_status_time` (`status`, `create_time`)
) ENGINE=InnoDB COMMENT='任务奖励钱包结算记录';

INSERT INTO `mission_goods`(`goods_type`, `goods_code`, `title`, `description`, `currency`, `min_amount`, `max_amount`, `display_rate`, `cycle_days`, `risk_level`, `sort_order`, `status`, `creator`, `updater`, `create_time`, `update_time`)
VALUES ('INVEST_PROJECT', 'INVEST-DEMO-001', '基础任务计划', '投资项目展示口径：用于说明任务中心计划，不产生独立投资扣款、派息或到期结算。', 'USDT', 50.000000, 500.000000, 1.2000, 7, 'LOW', 1, 1, '', '', 1782880000000, NULL),
       ('INVEST_PROJECT', 'INVEST-DEMO-002', '进阶任务计划', '收益最终以任务审核通过后的钱包奖励为准，本项目只做展示配置。', 'USDT', 100.000000, 1500.000000, 1.8000, 15, 'MEDIUM', 2, 1, '', '', 1782880000000, NULL),
       ('TASK_PRODUCT', 'TASK-PRODUCT-001', '新人任务商品', '任务中心普通商品展示配置，可关联运营任务。', 'USDT', 0.000000, 0.000000, 0.0000, 0, 'LOW', 10, 1, '', '', 1782880000000, NULL)
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`),
                        `description` = VALUES(`description`),
                        `currency` = VALUES(`currency`),
                        `min_amount` = VALUES(`min_amount`),
                        `max_amount` = VALUES(`max_amount`),
                        `display_rate` = VALUES(`display_rate`),
                        `cycle_days` = VALUES(`cycle_days`),
                        `risk_level` = VALUES(`risk_level`),
                        `sort_order` = VALUES(`sort_order`),
                        `status` = VALUES(`status`),
                        `update_time` = VALUES(`create_time`);

INSERT INTO `mission_task`(`task_code`, `title`, `description`, `task_type`, `currency`, `reward_amount`, `required_vip_level`, `daily_limit`, `start_at`, `end_at`, `sort_order`, `status`, `creator`, `updater`, `create_time`, `update_time`)
VALUES ('TASK-DEMO-001', '完成新手资料任务', '领取任务后提交完成说明，后台审核通过后奖励入账。', 'GENERAL', 'USDT', 1.000000, 0, 0, NULL, NULL, 1, 1, '', '', 1782880000000, NULL),
       ('TASK-DEMO-002', '提交任务计划截图', '提交任务证明后由后台审核，审核通过走钱包 TASK_REWARD 入账。', 'TASK_CENTER', 'USDT', 2.500000, 0, 0, NULL, NULL, 2, 1, '', '', 1782880000000, NULL)
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`),
                        `description` = VALUES(`description`),
                        `task_type` = VALUES(`task_type`),
                        `currency` = VALUES(`currency`),
                        `reward_amount` = VALUES(`reward_amount`),
                        `required_vip_level` = VALUES(`required_vip_level`),
                        `daily_limit` = VALUES(`daily_limit`),
                        `sort_order` = VALUES(`sort_order`),
                        `status` = VALUES(`status`),
                        `update_time` = VALUES(`create_time`);