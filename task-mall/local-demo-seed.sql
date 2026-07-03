-- =============================================================
-- task-mall 本地演示数据幂等补丁
--
-- 使用方式：mysql -uroot < local-demo-seed.sql
-- 约束：
-- - 只写演示固定 UID/订单号/记录号范围，不清空、不回滚已有业务数据。
-- - 所有写入均通过唯一键或固定演示 ID 段做 upsert，可重复执行。
-- - 钱包余额演示数据保持 total_balance = avail_balance + frozen_balance。
-- =============================================================

SET NAMES utf8mb4;
SET @seed_time := 1783050000000;
SET @demo_pwd_hash := '$2a$10$gZDw7vPVrfScGQcPxy50UeSQHa8il80RWo.bcE8z6NkcAJERMXqTW';

CREATE DATABASE IF NOT EXISTS `mall-admin` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall-user` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall-wallet` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall-mission` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall-promotion` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- =============================================================
-- mall-admin：会员菜单、角色菜单、内容、机器人配置
-- =============================================================
USE `mall-admin`;

CREATE TABLE IF NOT EXISTS `admin_bot_config`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `bot_name`    varchar(64)  NOT NULL COMMENT '机器人名称',
    `bot_token`   varchar(255) NOT NULL COMMENT '机器人Token',
    `webhook_url` varchar(255) NULL DEFAULT '' COMMENT 'Webhook地址',
    `description` varchar(255) NULL DEFAULT '' COMMENT '说明',
    `sort_order`  int          NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
    `status`      tinyint      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    `creator`     varchar(50)  NULL DEFAULT '' COMMENT '添加人',
    `updater`     varchar(50)  NULL DEFAULT '' COMMENT '最后修改人',
    `create_time` bigint       NULL DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time` bigint       NULL DEFAULT NULL COMMENT '数据最后修改时间（UTC毫秒）',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_bot_name`(`bot_name`) USING BTREE,
    INDEX `idx_status_sort`(`status`, `sort_order`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '后台机器人配置表';

CREATE TABLE IF NOT EXISTS `admin_bot_auto_reply`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `bot_id`        bigint       NOT NULL COMMENT '机器人ID',
    `keyword`       varchar(128) NOT NULL COMMENT '关键词',
    `reply_content` text         NOT NULL COMMENT '回复内容',
    `match_type`    tinyint      NOT NULL DEFAULT 1 COMMENT '匹配方式：1-包含，2-完全匹配',
    `sort_order`    int          NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
    `status`        tinyint      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    `creator`       varchar(50)  NULL DEFAULT '' COMMENT '添加人',
    `updater`       varchar(50)  NULL DEFAULT '' COMMENT '最后修改人',
    `create_time`   bigint       NULL DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time`   bigint       NULL DEFAULT NULL COMMENT '数据最后修改时间（UTC毫秒）',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_bot_status_sort`(`bot_id`, `status`, `sort_order`) USING BTREE,
    INDEX `idx_keyword`(`keyword`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '后台机器人自动回复表';

INSERT INTO `manage_menu_info`(`menu_key`, `name`, `path`, `type`, `component`, `hide_in_menu`, `icon`, `pno`, `menu_level`, `order`, `status`, `creator`, `updater`, `create_time`, `update_time`) VALUES
('menu.user-member.member-groups', 'member-groups', '/member-groups', 0, 'user-member/member-feature/index', 1, '', 6, 2, 1, 1, '', '', @seed_time, NULL),
('menu.user-member.member-log', 'member-log', '/member-log', 0, 'user-member/member-feature/index', 1, '', 6, 2, 2, 1, '', '', @seed_time, NULL),
('menu.user-member.member-flow', 'member-flow', '/member-flow', 0, 'user-member/member-feature/index', 1, '', 6, 2, 3, 1, '', '', @seed_time, NULL),
('menu.user-member.site-message', 'site-message', '/site-message', 0, 'user-member/member-feature/index', 1, '', 6, 2, 4, 1, '', '', @seed_time, NULL),
('menu.user-member.telegram-info', 'telegram-info', '/telegram-info', 0, 'user-member/member-feature/index', 1, '', 6, 2, 5, 1, '', '', @seed_time, NULL),
('menu.user-member.bot-config', 'bot-config', '/bot-config', 0, 'user-member/bot-config/index', 1, '', 6, 2, 6, 1, '', '', @seed_time, NULL),
('menu.user-member.balance-record', 'balance-record', '/balance-record', 0, 'user-member/member-feature/index', 1, '', 6, 2, 7, 1, '', '', @seed_time, NULL),
('menu.user-member.vip-upgrade-record', 'vip-upgrade-record', '/vip-upgrade-record', 0, 'user-member/member-feature/index', 1, '', 6, 2, 8, 1, '', '', @seed_time, NULL),
('menu.user-member.share-audit', 'share-audit', '/share-audit', 0, 'user-member/member-feature/index', 1, '', 6, 2, 9, 1, '', '', @seed_time, NULL),
('menu.user-member.test-account', 'test-account', '/test-account', 0, 'user-member/member-feature/index', 1, '', 6, 2, 10, 1, '', '', @seed_time, NULL),
('menu.user-member.vip-config', 'vip-config', '/vip-config', 0, 'user-member/vip-config/index', 1, '', 6, 2, 11, 1, '', '', @seed_time, NULL),
('menu.user-member.invite-commission', 'invite-commission', '/invite-commission', 0, 'user-member/invite-commission/index', 1, '', 6, 2, 12, 1, '', '', @seed_time, NULL)
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `path` = VALUES(`path`),
    `type` = VALUES(`type`),
    `component` = VALUES(`component`),
    `hide_in_menu` = VALUES(`hide_in_menu`),
    `icon` = VALUES(`icon`),
    `pno` = VALUES(`pno`),
    `menu_level` = VALUES(`menu_level`),
    `order` = VALUES(`order`),
    `status` = VALUES(`status`),
    `update_time` = @seed_time;

INSERT INTO `manage_role_menu`(`role_code`, `menu_key`, `creator`, `updater`, `create_time`, `update_time`)
SELECT 'ROLE_ADMIN', m.`menu_key`, '', '', @seed_time, NULL
FROM `manage_menu_info` m
WHERE m.`status` = 1
ON DUPLICATE KEY UPDATE `update_time` = @seed_time;

INSERT INTO `admin_system_param`(`param_key`, `param_value`, `description`, `sort_order`, `status`, `creator`, `updater`, `create_time`, `update_time`) VALUES
('demo.customer_service', '在线客服配置待接入，当前演示环境只展示入口。', '用户端客服入口演示文案', 100, 1, '', '', @seed_time, NULL),
('demo.readonly_notice', '当前为后台模拟登录，仅可查看，不能操作', '后台模拟登录只读提示', 101, 1, '', '', @seed_time, NULL)
ON DUPLICATE KEY UPDATE
    `param_value` = VALUES(`param_value`),
    `description` = VALUES(`description`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `update_time` = @seed_time;

INSERT INTO `admin_content_item`(`id`, `type`, `language_code`, `title`, `summary`, `content`, `sort_order`, `status`, `creator`, `updater`, `create_time`, `update_time`) VALUES
(90001, 'NOTICE', 'zh-CN', '演示环境公告', '用于本地 H5 公告页展示', '本地演示环境已初始化会员、钱包、任务、抽奖和机器人配置数据。资金类操作请使用演示账号验证，不要接入真实链上资金。', 1, 1, '', '', @seed_time, NULL),
(90002, 'NOTICE', 'zh-CN', '模拟登录只读说明', '后台模拟登录只允许查看', '通过后台模拟登录进入用户端时，充值地址分配、复制、提现、转账、任务提交、VIP 升级和抽奖均会被拦截。提示文案：当前为后台模拟登录，仅可查看，不能操作。', 2, 1, '', '', @seed_time, NULL),
(90003, 'USER_AGREEMENT', 'zh-CN', '用户协议', '本地演示协议', '本协议用于本地演示页面展示。平台账户、任务、钱包和活动能力以系统内实际接口状态为准。', 1, 1, '', '', @seed_time, NULL),
(90004, 'USER_PRIVACY', 'zh-CN', '隐私政策', '本地演示隐私政策', '本地演示环境不应保存真实用户端 token、真实链上私钥或第三方 API Key。', 1, 1, '', '', @seed_time, NULL),
(90005, 'COMPANY_PROFILE', 'zh-CN', '公司简介', '本地演示公司简介', '环宇出海任务平台本地演示内容，用于后台内容配置和用户端内容页校准。', 1, 1, '', '', @seed_time, NULL),
(90006, 'PLATFORM_PROFILE', 'zh-CN', '平台介绍', '本地演示平台介绍', '平台演示任务、钱包、会员、VIP、邀请和抽奖闭环。未接入的能力以待开放状态页展示，不伪造数据。', 1, 1, '', '', @seed_time, NULL)
ON DUPLICATE KEY UPDATE
    `type` = VALUES(`type`),
    `language_code` = VALUES(`language_code`),
    `title` = VALUES(`title`),
    `summary` = VALUES(`summary`),
    `content` = VALUES(`content`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `update_time` = @seed_time;

INSERT INTO `admin_bot_config`(`id`, `bot_name`, `bot_token`, `webhook_url`, `description`, `sort_order`, `status`, `creator`, `updater`, `create_time`, `update_time`) VALUES
(1, 'Demo Telegram Bot', '123456789:demo_token_for_local_seed_only', '', '本地演示机器人配置，不调用 Telegram API', 0, 1, '', '', @seed_time, NULL)
ON DUPLICATE KEY UPDATE
    `bot_token` = VALUES(`bot_token`),
    `webhook_url` = VALUES(`webhook_url`),
    `description` = VALUES(`description`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `update_time` = @seed_time;

INSERT INTO `admin_bot_auto_reply`(`id`, `bot_id`, `keyword`, `reply_content`, `match_type`, `sort_order`, `status`, `creator`, `updater`, `create_time`, `update_time`) VALUES
(1, (SELECT `id` FROM `admin_bot_config` WHERE `bot_name` = 'Demo Telegram Bot' LIMIT 1), '/start', '欢迎使用任务平台，请在前台页面查看任务和钱包。', 2, 0, 1, '', '', @seed_time, NULL),
(2, (SELECT `id` FROM `admin_bot_config` WHERE `bot_name` = 'Demo Telegram Bot' LIMIT 1), '客服', '如需帮助，请联系在线客服。', 1, 10, 1, '', '', @seed_time, NULL)
ON DUPLICATE KEY UPDATE
    `bot_id` = VALUES(`bot_id`),
    `keyword` = VALUES(`keyword`),
    `reply_content` = VALUES(`reply_content`),
    `match_type` = VALUES(`match_type`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `update_time` = @seed_time;

-- =============================================================
-- mall-user：VIP0-VIP5、演示会员、上下级、分组、升级和返佣记录
-- =============================================================
USE `mall-user`;

CREATE TABLE IF NOT EXISTS `user_member_group`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `group_name`  varchar(50)  NOT NULL COMMENT '分组名称',
    `remark`      varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
    `status`      tinyint      NOT NULL DEFAULT 1 COMMENT '状态（1-启用，0-停用）',
    `sort_order`  int          NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` bigint                DEFAULT NULL COMMENT '数据创建时间',
    `update_time` bigint                DEFAULT NULL COMMENT '数据修改时间',
    `creator`     varchar(50)           DEFAULT '' COMMENT '数据创建者',
    `updater`     varchar(50)           DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_name` (`group_name`),
    KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB COMMENT='会员分组表';

CREATE TABLE IF NOT EXISTS `user_member_group_bind`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     bigint      NOT NULL COMMENT '全局唯一用户ID',
    `group_id`    bigint      NOT NULL COMMENT '会员分组ID',
    `create_time` bigint      DEFAULT NULL COMMENT '数据创建时间',
    `update_time` bigint      DEFAULT NULL COMMENT '数据修改时间',
    `creator`     varchar(50) DEFAULT '' COMMENT '数据创建者',
    `updater`     varchar(50) DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB COMMENT='会员分组绑定表';

INSERT INTO `user_member_group`(`group_name`, `remark`, `status`, `sort_order`, `creator`, `updater`, `create_time`, `update_time`) VALUES
('普通会员', '默认演示分组', 1, 1, '', '', @seed_time, NULL),
('重点会员', '高价值会员演示分组', 1, 2, '', '', @seed_time, NULL),
('测试会员', '测试账号和演示账号分组', 1, 3, '', '', @seed_time, NULL)
ON DUPLICATE KEY UPDATE
    `remark` = VALUES(`remark`),
    `status` = VALUES(`status`),
    `sort_order` = VALUES(`sort_order`),
    `update_time` = @seed_time;

INSERT INTO `vip_level_config`(`level`, `level_name`, `price`, `rebate_rate`, `daily_tasks`, `benefits`, `sort_order`, `status`, `creator`, `updater`, `create_time`, `update_time`) VALUES
(0, 'VIP0', 0.000000, 0.0000, 2, '基础会员\n可查看任务和钱包记录', 0, 1, '', '', @seed_time, NULL),
(1, 'VIP1', 100.000000, 0.0100, 5, '每日任务数 5\n基础返佣比例 1%', 1, 1, '', '', @seed_time, NULL),
(2, 'VIP2', 300.000000, 0.0200, 10, '每日任务数 10\n基础返佣比例 2%', 2, 1, '', '', @seed_time, NULL),
(3, 'VIP3', 800.000000, 0.0300, 20, '每日任务数 20\n基础返佣比例 3%', 3, 1, '', '', @seed_time, NULL),
(4, 'VIP4', 1500.000000, 0.0400, 35, '每日任务数 35\n基础返佣比例 4%', 4, 1, '', '', @seed_time, NULL),
(5, 'VIP5', 3000.000000, 0.0500, 50, '每日任务数 50\n基础返佣比例 5%', 5, 1, '', '', @seed_time, NULL)
ON DUPLICATE KEY UPDATE
    `level_name` = VALUES(`level_name`),
    `price` = VALUES(`price`),
    `rebate_rate` = VALUES(`rebate_rate`),
    `daily_tasks` = VALUES(`daily_tasks`),
    `benefits` = VALUES(`benefits`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `update_time` = @seed_time;

INSERT INTO `user_info`(`user_id`, `email`, `nickname`, `invite_code`, `vip_level`, `inviter`, `status`, `create_time`, `update_time`, `creator`, `updater`) VALUES
(178288000001, 'demo_vip0@example.test', '演示会员VIP0', 'DEMO001', 0, NULL, 1, @seed_time, NULL, '', ''),
(178288000002, 'demo_vip1@example.test', '演示会员VIP1', 'DEMO002', 1, 178288000001, 1, @seed_time, NULL, '', ''),
(178288000003, 'demo_vip2@example.test', '演示会员VIP2', 'DEMO003', 2, 178288000001, 1, @seed_time, NULL, '', ''),
(178288000004, 'demo_vip3@example.test', '演示会员VIP3', 'DEMO004', 3, 178288000002, 1, @seed_time, NULL, '', ''),
(178288000005, 'demo_vip4@example.test', '演示会员VIP4', 'DEMO005', 4, 178288000002, 1, @seed_time, NULL, '', ''),
(178288000006, 'demo_vip5@example.test', '演示会员VIP5', 'DEMO006', 5, 178288000003, 2, @seed_time, NULL, '', '')
ON DUPLICATE KEY UPDATE
    `email` = VALUES(`email`),
    `nickname` = VALUES(`nickname`),
    `vip_level` = VALUES(`vip_level`),
    `inviter` = VALUES(`inviter`),
    `status` = VALUES(`status`),
    `update_time` = @seed_time;

INSERT INTO `user_account`(`user_id`, `account`, `password_hash`, `create_time`, `update_time`, `creator`, `updater`) VALUES
(178288000001, 'demo_vip0', @demo_pwd_hash, @seed_time, NULL, '', ''),
(178288000002, 'demo_vip1', @demo_pwd_hash, @seed_time, NULL, '', ''),
(178288000003, 'demo_vip2', @demo_pwd_hash, @seed_time, NULL, '', ''),
(178288000004, 'demo_vip3', @demo_pwd_hash, @seed_time, NULL, '', ''),
(178288000005, 'demo_vip4', @demo_pwd_hash, @seed_time, NULL, '', ''),
(178288000006, 'demo_vip5_frozen', @demo_pwd_hash, @seed_time, NULL, '', '')
ON DUPLICATE KEY UPDATE
    `account` = VALUES(`account`),
    `password_hash` = VALUES(`password_hash`),
    `update_time` = @seed_time;

INSERT INTO `user_invite_relation`(`user_id`, `inviter_id`, `invite_code`, `create_time`, `update_time`, `creator`, `updater`) VALUES
(178288000002, 178288000001, 'DEMO001', @seed_time, NULL, '', ''),
(178288000003, 178288000001, 'DEMO001', @seed_time, NULL, '', ''),
(178288000004, 178288000002, 'DEMO002', @seed_time, NULL, '', ''),
(178288000005, 178288000002, 'DEMO002', @seed_time, NULL, '', ''),
(178288000006, 178288000003, 'DEMO003', @seed_time, NULL, '', '')
ON DUPLICATE KEY UPDATE
    `inviter_id` = VALUES(`inviter_id`),
    `invite_code` = VALUES(`invite_code`),
    `update_time` = @seed_time;

INSERT INTO `user_member_group_bind`(`user_id`, `group_id`, `create_time`, `update_time`, `creator`, `updater`)
SELECT 178288000001, `id`, @seed_time, NULL, '', '' FROM `user_member_group` WHERE `group_name` = '普通会员'
ON DUPLICATE KEY UPDATE `group_id` = VALUES(`group_id`), `update_time` = @seed_time;
INSERT INTO `user_member_group_bind`(`user_id`, `group_id`, `create_time`, `update_time`, `creator`, `updater`)
SELECT 178288000002, `id`, @seed_time, NULL, '', '' FROM `user_member_group` WHERE `group_name` = '重点会员'
ON DUPLICATE KEY UPDATE `group_id` = VALUES(`group_id`), `update_time` = @seed_time;
INSERT INTO `user_member_group_bind`(`user_id`, `group_id`, `create_time`, `update_time`, `creator`, `updater`)
SELECT 178288000003, `id`, @seed_time, NULL, '', '' FROM `user_member_group` WHERE `group_name` = '重点会员'
ON DUPLICATE KEY UPDATE `group_id` = VALUES(`group_id`), `update_time` = @seed_time;
INSERT INTO `user_member_group_bind`(`user_id`, `group_id`, `create_time`, `update_time`, `creator`, `updater`)
SELECT 178288000004, `id`, @seed_time, NULL, '', '' FROM `user_member_group` WHERE `group_name` = '普通会员'
ON DUPLICATE KEY UPDATE `group_id` = VALUES(`group_id`), `update_time` = @seed_time;
INSERT INTO `user_member_group_bind`(`user_id`, `group_id`, `create_time`, `update_time`, `creator`, `updater`)
SELECT 178288000005, `id`, @seed_time, NULL, '', '' FROM `user_member_group` WHERE `group_name` = '测试会员'
ON DUPLICATE KEY UPDATE `group_id` = VALUES(`group_id`), `update_time` = @seed_time;
INSERT INTO `user_member_group_bind`(`user_id`, `group_id`, `create_time`, `update_time`, `creator`, `updater`)
SELECT 178288000006, `id`, @seed_time, NULL, '', '' FROM `user_member_group` WHERE `group_name` = '测试会员'
ON DUPLICATE KEY UPDATE `group_id` = VALUES(`group_id`), `update_time` = @seed_time;

INSERT INTO `vip_upgrade_order`(`order_no`, `user_id`, `from_level`, `to_level`, `currency`, `amount`, `status`, `wallet_flow_no`, `finished_at`, `remark`, `create_time`, `update_time`, `creator`, `updater`) VALUES
('VIP-DEMO-SUCCESS-002', 178288000002, 0, 1, 'USDT', 100.000000, 'SUCCESS', 'FLOW-DEMO-VIP-002', @seed_time, '演示 VIP1 升级成功记录', @seed_time, NULL, '', ''),
('VIP-DEMO-PENDING-005', 178288000005, 3, 4, 'USDT', 1500.000000, 'PENDING', NULL, NULL, '演示待支付 VIP4 升级记录', @seed_time, NULL, '', '')
ON DUPLICATE KEY UPDATE
    `from_level` = VALUES(`from_level`),
    `to_level` = VALUES(`to_level`),
    `currency` = VALUES(`currency`),
    `amount` = VALUES(`amount`),
    `status` = VALUES(`status`),
    `wallet_flow_no` = VALUES(`wallet_flow_no`),
    `finished_at` = VALUES(`finished_at`),
    `remark` = VALUES(`remark`),
    `update_time` = @seed_time;

INSERT INTO `invite_commission_record`(`record_no`, `inviter_user_id`, `source_user_id`, `source_order_no`, `business_type`, `currency`, `source_amount`, `commission_rate`, `commission_amount`, `status`, `wallet_flow_no`, `fail_reason`, `settled_at`, `create_time`, `update_time`, `creator`, `updater`) VALUES
('IC-DEMO-001', 178288000001, 178288000002, 'VIP-DEMO-SUCCESS-002', 'VIP_UPGRADE', 'USDT', 100.000000, 0.0560, 5.600000, 'SETTLED', 'FLOW-DEMO-INVITE-001', NULL, @seed_time, @seed_time, NULL, '', ''),
('IC-DEMO-002', 178288000002, 178288000004, 'R-DEMO-004', 'RECHARGE', 'USDT', 1250.000000, 0.0020, 2.500000, 'SETTLED', 'FLOW-DEMO-INVITE-002', NULL, @seed_time, @seed_time, NULL, '', '')
ON DUPLICATE KEY UPDATE
    `inviter_user_id` = VALUES(`inviter_user_id`),
    `source_user_id` = VALUES(`source_user_id`),
    `source_amount` = VALUES(`source_amount`),
    `commission_rate` = VALUES(`commission_rate`),
    `commission_amount` = VALUES(`commission_amount`),
    `status` = VALUES(`status`),
    `wallet_flow_no` = VALUES(`wallet_flow_no`),
    `fail_reason` = VALUES(`fail_reason`),
    `settled_at` = VALUES(`settled_at`),
    `update_time` = @seed_time;

-- =============================================================
-- mall-wallet：账户、地址、充值、提现、转账、账变流水
-- =============================================================
USE `mall-wallet`;

INSERT INTO `chain_config` (`chain_code`, `chain_id`, `rpc_ref`, `explorer_ref`, `min_confirmations`, `enabled`) VALUES
('TRON', NULL, 'wallet.tron.grpc-endpoint', 'wallet.tron.endpoint', 19, 1),
('ETH', 1, 'wallet.evm.chains.ETH.rpc-url', NULL, 12, 0),
('BSC', 56, 'wallet.evm.chains.BSC.rpc-url', NULL, 15, 0),
('POLYGON', 137, 'wallet.evm.chains.POLYGON.rpc-url', NULL, 128, 0)
ON DUPLICATE KEY UPDATE
    `chain_id` = VALUES(`chain_id`),
    `rpc_ref` = VALUES(`rpc_ref`),
    `explorer_ref` = VALUES(`explorer_ref`),
    `min_confirmations` = VALUES(`min_confirmations`),
    `enabled` = VALUES(`enabled`),
    `update_time` = @seed_time;

INSERT INTO `coin_config` (`chain_code`, `symbol`, `contract_address`, `decimals`, `enabled`) VALUES
('TRON', 'USDT', 'TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t', 6, 1),
('ETH', 'USDT', '0xdAC17F958D2ee523a2206206994597C13D831ec7', 6, 1),
('BSC', 'USDT', '0x55d398326f99059fF775485246999027B3197955', 18, 1),
('POLYGON', 'USDT', '0xc2132D05D31c914a87C6611C10748AEb04B58e8F', 6, 1)
ON DUPLICATE KEY UPDATE
    `contract_address` = VALUES(`contract_address`),
    `decimals` = VALUES(`decimals`),
    `enabled` = VALUES(`enabled`),
    `update_time` = @seed_time;

INSERT INTO `wallet_account`(`user_id`, `currency`, `total_balance`, `avail_balance`, `frozen_balance`, `create_time`, `update_time`, `creator`, `updater`) VALUES
(178288000001, 'USDT', 494.100000, 474.100000, 20.000000, @seed_time, NULL, '', ''),
(178288000002, 'USDT', 117.500000, 117.500000, 0.000000, @seed_time, NULL, '', ''),
(178288000003, 'USDT', 817.500000, 817.500000, 0.000000, @seed_time, NULL, '', ''),
(178288000004, 'USDT', 1250.000000, 1250.000000, 0.000000, @seed_time, NULL, '', ''),
(178288000005, 'USDT', 2400.000000, 2340.000000, 60.000000, @seed_time, NULL, '', ''),
(178288000006, 'USDT', 5500.000000, 5500.000000, 0.000000, @seed_time, NULL, '', '')
ON DUPLICATE KEY UPDATE
    `total_balance` = VALUES(`total_balance`),
    `avail_balance` = VALUES(`avail_balance`),
    `frozen_balance` = VALUES(`frozen_balance`),
    `update_time` = @seed_time;

INSERT INTO `user_deposit_address`(`user_id`, `chain_code`, `address`, `enc_priv_key`, `create_time`, `update_time`, `creator`, `updater`) VALUES
(178288000001, 'TRON', 'TDEMOAddress0000000000000000000001', 'demo-ciphertext-not-for-chain-use-001', @seed_time, NULL, '', ''),
(178288000002, 'TRON', 'TDEMOAddress0000000000000000000002', 'demo-ciphertext-not-for-chain-use-002', @seed_time, NULL, '', ''),
(178288000003, 'TRON', 'TDEMOAddress0000000000000000000003', 'demo-ciphertext-not-for-chain-use-003', @seed_time, NULL, '', ''),
(178288000004, 'TRON', 'TDEMOAddress0000000000000000000004', 'demo-ciphertext-not-for-chain-use-004', @seed_time, NULL, '', ''),
(178288000005, 'TRON', 'TDEMOAddress0000000000000000000005', 'demo-ciphertext-not-for-chain-use-005', @seed_time, NULL, '', ''),
(178288000006, 'TRON', 'TDEMOAddress0000000000000000000006', 'demo-ciphertext-not-for-chain-use-006', @seed_time, NULL, '', '')
ON DUPLICATE KEY UPDATE
    `address` = VALUES(`address`),
    `enc_priv_key` = VALUES(`enc_priv_key`),
    `update_time` = @seed_time;

INSERT INTO `wallet_recharge_order`(`order_no`, `user_id`, `chain_code`, `coin`, `amount`, `from_address`, `to_address`, `tx_hash`, `log_index`, `confirmations`, `status`, `credited_at`, `create_time`, `update_time`, `creator`, `updater`) VALUES
('R-DEMO-001', 178288000001, 'TRON', 'USDT', 500.000000, 'TDEMOFromAddress0000000000000000001', 'TDEMOAddress0000000000000000000001', 'demo_recharge_tx_001', 0, 20, 'CREDITED', @seed_time, @seed_time, NULL, '', ''),
('R-DEMO-002', 178288000002, 'TRON', 'USDT', 200.000000, 'TDEMOFromAddress0000000000000000002', 'TDEMOAddress0000000000000000000002', 'demo_recharge_tx_002', 0, 20, 'CREDITED', @seed_time, @seed_time, NULL, '', ''),
('R-DEMO-003', 178288000003, 'TRON', 'USDT', 800.000000, 'TDEMOFromAddress0000000000000000003', 'TDEMOAddress0000000000000000000003', 'demo_recharge_tx_003', 0, 20, 'CREDITED', @seed_time, @seed_time, NULL, '', ''),
('R-DEMO-004', 178288000004, 'TRON', 'USDT', 1250.000000, 'TDEMOFromAddress0000000000000000004', 'TDEMOAddress0000000000000000000004', 'demo_recharge_tx_004', 0, 20, 'CREDITED', @seed_time, @seed_time, NULL, '', ''),
('R-DEMO-005', 178288000005, 'TRON', 'USDT', 2400.000000, 'TDEMOFromAddress0000000000000000005', 'TDEMOAddress0000000000000000000005', 'demo_recharge_tx_005', 0, 20, 'CREDITED', @seed_time, @seed_time, NULL, '', ''),
('R-DEMO-006', 178288000006, 'TRON', 'USDT', 5500.000000, 'TDEMOFromAddress0000000000000000006', 'TDEMOAddress0000000000000000000006', 'demo_recharge_tx_006', 0, 20, 'CREDITED', @seed_time, @seed_time, NULL, '', ''),
('R-DEMO-PENDING-001', 178288000001, 'TRON', 'USDT', 75.000000, NULL, 'TDEMOAddress0000000000000000000001', 'demo_recharge_tx_pending_001', 0, 0, 'PENDING', NULL, @seed_time, NULL, '', '')
ON DUPLICATE KEY UPDATE
    `user_id` = VALUES(`user_id`),
    `amount` = VALUES(`amount`),
    `from_address` = VALUES(`from_address`),
    `to_address` = VALUES(`to_address`),
    `confirmations` = VALUES(`confirmations`),
    `status` = VALUES(`status`),
    `credited_at` = VALUES(`credited_at`),
    `update_time` = @seed_time;

INSERT INTO `payment_order`(`order_no`, `user_id`, `business_type`, `business_order_no`, `channel_code`, `channel_order_no`, `currency`, `amount`, `status`, `pay_address`, `payer_address`, `tx_hash`, `audit_remark`, `paid_at`, `expired_at`, `creator`, `updater`, `create_time`, `update_time`) VALUES
('PAY-DEMO-RECHARGE-001', 178288000001, 'RECHARGE', 'R-DEMO-001', 'TRON', 'TRON-DEMO-001', 'USDT', 500.000000, 'PAID', 'TDEMOAddress0000000000000000000001', 'TDEMOFromAddress0000000000000000001', 'demo_recharge_tx_001', '充值已入账演示审计单', @seed_time, NULL, '', '', @seed_time, NULL),
('PAY-DEMO-RECHARGE-PENDING', 178288000001, 'RECHARGE', 'R-DEMO-PENDING-001', 'TRON', 'TRON-DEMO-PENDING-001', 'USDT', 75.000000, 'PENDING', 'TDEMOAddress0000000000000000000001', NULL, NULL, '充值待确认演示审计单，不改变余额', NULL, 1783136400000, '', '', @seed_time, NULL),
('PAY-DEMO-VIP-002', 178288000002, 'VIP_UPGRADE', 'VIP-DEMO-SUCCESS-002', 'MANUAL', 'MANUAL-VIP-DEMO-002', 'USDT', 100.000000, 'PAID', 'manual-counter', NULL, 'manual_vip_demo_002', 'VIP 升级支付审计演示单', @seed_time, NULL, '', '', @seed_time, NULL)
ON DUPLICATE KEY UPDATE
    `business_type` = VALUES(`business_type`),
    `business_order_no` = VALUES(`business_order_no`),
    `channel_code` = VALUES(`channel_code`),
    `channel_order_no` = VALUES(`channel_order_no`),
    `currency` = VALUES(`currency`),
    `amount` = VALUES(`amount`),
    `status` = VALUES(`status`),
    `pay_address` = VALUES(`pay_address`),
    `payer_address` = VALUES(`payer_address`),
    `tx_hash` = VALUES(`tx_hash`),
    `audit_remark` = VALUES(`audit_remark`),
    `paid_at` = VALUES(`paid_at`),
    `expired_at` = VALUES(`expired_at`),
    `update_time` = @seed_time;

INSERT INTO `wallet_withdraw_order`(`order_no`, `user_id`, `chain_code`, `coin`, `amount`, `fee`, `receive_amount`, `to_address`, `from_address`, `tx_hash`, `confirmations`, `status`, `reviewer`, `review_remark`, `reviewed_at`, `broadcast_at`, `finished_at`, `create_time`, `update_time`, `creator`, `updater`) VALUES
('W-DEMO-REVIEW-001', 178288000001, 'TRON', 'USDT', 20.000000, 1.000000, 19.000000, 'TDEMOWithdrawTo00000000000000001', NULL, NULL, 0, 'REVIEWING', NULL, '演示审核中提现，金额已冻结', NULL, NULL, NULL, @seed_time, NULL, '', ''),
('W-DEMO-CONFIRMED-003', 178288000003, 'TRON', 'USDT', 10.000000, 1.000000, 9.000000, 'TDEMOWithdrawTo00000000000000003', 'TDEMOHotWallet000000000000000003', 'demo_withdraw_tx_003', 20, 'CONFIRMED', 'admin', '演示提现已确认', @seed_time, @seed_time, @seed_time, @seed_time, NULL, '', ''),
('W-DEMO-REJECTED-002', 178288000002, 'TRON', 'USDT', 10.000000, 1.000000, 9.000000, 'TDEMOWithdrawTo00000000000000002', NULL, NULL, 0, 'REJECTED', 'admin', '演示提现驳回，未冻结资金', @seed_time, NULL, @seed_time, @seed_time, NULL, '', ''),
('W-DEMO-REVIEW-005', 178288000005, 'TRON', 'USDT', 60.000000, 2.000000, 58.000000, 'TDEMOWithdrawTo00000000000000005', NULL, NULL, 0, 'REVIEWING', NULL, '演示大额提现审核中，金额已冻结', NULL, NULL, NULL, @seed_time, NULL, '', '')
ON DUPLICATE KEY UPDATE
    `amount` = VALUES(`amount`),
    `fee` = VALUES(`fee`),
    `receive_amount` = VALUES(`receive_amount`),
    `to_address` = VALUES(`to_address`),
    `from_address` = VALUES(`from_address`),
    `tx_hash` = VALUES(`tx_hash`),
    `confirmations` = VALUES(`confirmations`),
    `status` = VALUES(`status`),
    `reviewer` = VALUES(`reviewer`),
    `review_remark` = VALUES(`review_remark`),
    `reviewed_at` = VALUES(`reviewed_at`),
    `broadcast_at` = VALUES(`broadcast_at`),
    `finished_at` = VALUES(`finished_at`),
    `update_time` = @seed_time;

INSERT INTO `wallet_transfer_order`(`order_no`, `from_user_id`, `to_user_id`, `coin`, `amount`, `status`, `remark`, `finished_at`, `create_time`, `update_time`, `creator`, `updater`) VALUES
('T-DEMO-001-002', 178288000001, 178288000002, 'USDT', 15.000000, 'SUCCESS', '演示会员转账 001 -> 002', @seed_time, @seed_time, NULL, '', ''),
('T-DEMO-002-003', 178288000002, 178288000003, 'USDT', 25.000000, 'SUCCESS', '演示会员转账 002 -> 003', @seed_time, @seed_time, NULL, '', '')
ON DUPLICATE KEY UPDATE
    `from_user_id` = VALUES(`from_user_id`),
    `to_user_id` = VALUES(`to_user_id`),
    `amount` = VALUES(`amount`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`),
    `finished_at` = VALUES(`finished_at`),
    `update_time` = @seed_time;

INSERT INTO `wallet_flow_detail`(`flow_no`, `user_id`, `wallet_id`, `biz_type`, `biz_id`, `direction`, `change_amt`, `balance_before`, `balance_after`, `remark`, `create_time`, `update_time`, `creator`, `updater`) VALUES
('FLOW-DEMO-R-001', 178288000001, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000001 AND `currency` = 'USDT' LIMIT 1), 'RECHARGE', 'R-DEMO-001', 'IN', 500.000000, 0.000000, 500.000000, '演示充值入账', @seed_time, NULL, '', ''),
('FLOW-DEMO-TASK-001', 178288000001, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000001 AND `currency` = 'USDT' LIMIT 1), 'TASK_REWARD', 'MUT-DEMO-APPROVED-001', 'IN', 3.000000, 500.000000, 503.000000, '演示任务奖励入账', @seed_time, NULL, '', ''),
('FLOW-DEMO-INVITE-001', 178288000001, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000001 AND `currency` = 'USDT' LIMIT 1), 'INVITE_COMMISSION', 'IC-DEMO-001', 'IN', 5.600000, 503.000000, 508.600000, '演示邀请返佣入账', @seed_time, NULL, '', ''),
('FLOW-DEMO-LOTTERY-001', 178288000001, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000001 AND `currency` = 'USDT' LIMIT 1), 'LOTTERY_REWARD', 'LOTTERY-DEMO-001', 'IN', 0.500000, 508.600000, 509.100000, '演示抽奖现金奖励入账', @seed_time, NULL, '', ''),
('FLOW-DEMO-TRANSFER-OUT-001', 178288000001, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000001 AND `currency` = 'USDT' LIMIT 1), 'TRANSFER_OUT', 'T-DEMO-001-002', 'OUT', 15.000000, 509.100000, 494.100000, '演示转账转出', @seed_time, NULL, '', ''),
('FLOW-DEMO-WITHDRAW-FREEZE-001', 178288000001, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000001 AND `currency` = 'USDT' LIMIT 1), 'WITHDRAW_FREEZE', 'W-DEMO-REVIEW-001', 'FREEZE', 20.000000, 494.100000, 474.100000, '演示提现冻结', @seed_time, NULL, '', ''),
('FLOW-DEMO-R-002', 178288000002, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000002 AND `currency` = 'USDT' LIMIT 1), 'RECHARGE', 'R-DEMO-002', 'IN', 200.000000, 0.000000, 200.000000, '演示充值入账', @seed_time, NULL, '', ''),
('FLOW-DEMO-TRANSFER-IN-002', 178288000002, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000002 AND `currency` = 'USDT' LIMIT 1), 'TRANSFER_IN', 'T-DEMO-001-002', 'IN', 15.000000, 200.000000, 215.000000, '演示转账转入', @seed_time, NULL, '', ''),
('FLOW-DEMO-VIP-002', 178288000002, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000002 AND `currency` = 'USDT' LIMIT 1), 'VIP_UPGRADE', 'VIP-DEMO-SUCCESS-002', 'OUT', 100.000000, 215.000000, 115.000000, '演示 VIP 升级扣款', @seed_time, NULL, '', ''),
('FLOW-DEMO-INVITE-002', 178288000002, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000002 AND `currency` = 'USDT' LIMIT 1), 'INVITE_COMMISSION', 'IC-DEMO-002', 'IN', 2.500000, 115.000000, 117.500000, '演示下级充值返佣入账', @seed_time, NULL, '', ''),
('FLOW-DEMO-R-003', 178288000003, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000003 AND `currency` = 'USDT' LIMIT 1), 'RECHARGE', 'R-DEMO-003', 'IN', 800.000000, 0.000000, 800.000000, '演示充值入账', @seed_time, NULL, '', ''),
('FLOW-DEMO-TASK-003', 178288000003, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000003 AND `currency` = 'USDT' LIMIT 1), 'TASK_REWARD', 'MUT-DEMO-APPROVED-003', 'IN', 2.500000, 800.000000, 802.500000, '演示任务奖励入账', @seed_time, NULL, '', ''),
('FLOW-DEMO-TRANSFER-IN-003', 178288000003, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000003 AND `currency` = 'USDT' LIMIT 1), 'TRANSFER_IN', 'T-DEMO-002-003', 'IN', 25.000000, 802.500000, 827.500000, '演示转账转入', @seed_time, NULL, '', ''),
('FLOW-DEMO-WITHDRAW-SETTLE-003', 178288000003, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000003 AND `currency` = 'USDT' LIMIT 1), 'WITHDRAW_SETTLE', 'W-DEMO-CONFIRMED-003', 'OUT', 10.000000, 827.500000, 817.500000, '演示提现确认扣减', @seed_time, NULL, '', ''),
('FLOW-DEMO-R-004', 178288000004, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000004 AND `currency` = 'USDT' LIMIT 1), 'RECHARGE', 'R-DEMO-004', 'IN', 1250.000000, 0.000000, 1250.000000, '演示充值入账', @seed_time, NULL, '', ''),
('FLOW-DEMO-R-005', 178288000005, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000005 AND `currency` = 'USDT' LIMIT 1), 'RECHARGE', 'R-DEMO-005', 'IN', 2400.000000, 0.000000, 2400.000000, '演示充值入账', @seed_time, NULL, '', ''),
('FLOW-DEMO-WITHDRAW-FREEZE-005', 178288000005, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000005 AND `currency` = 'USDT' LIMIT 1), 'WITHDRAW_FREEZE', 'W-DEMO-REVIEW-005', 'FREEZE', 60.000000, 2400.000000, 2340.000000, '演示提现冻结', @seed_time, NULL, '', ''),
('FLOW-DEMO-R-006', 178288000006, (SELECT `id` FROM `wallet_account` WHERE `user_id` = 178288000006 AND `currency` = 'USDT' LIMIT 1), 'RECHARGE', 'R-DEMO-006', 'IN', 5500.000000, 0.000000, 5500.000000, '演示充值入账', @seed_time, NULL, '', '')
ON DUPLICATE KEY UPDATE
    `user_id` = VALUES(`user_id`),
    `wallet_id` = VALUES(`wallet_id`),
    `biz_type` = VALUES(`biz_type`),
    `biz_id` = VALUES(`biz_id`),
    `direction` = VALUES(`direction`),
    `change_amt` = VALUES(`change_amt`),
    `balance_before` = VALUES(`balance_before`),
    `balance_after` = VALUES(`balance_after`),
    `remark` = VALUES(`remark`),
    `update_time` = @seed_time;

-- =============================================================
-- mall-mission：任务配置、用户任务状态、奖励结算
-- =============================================================
USE `mall-mission`;

INSERT INTO `mission_task`(`task_code`, `title`, `description`, `task_type`, `currency`, `reward_amount`, `required_vip_level`, `daily_limit`, `start_at`, `end_at`, `sort_order`, `status`, `creator`, `updater`, `create_time`, `update_time`) VALUES
('TASK-DEMO-001', '完成新手资料任务', '领取任务后提交完成说明，后台审核通过后奖励入账。', 'GENERAL', 'USDT', 1.000000, 0, 0, NULL, NULL, 1, 1, '', '', @seed_time, NULL),
('TASK-DEMO-002', '提交任务计划截图', '提交任务证明后由后台审核，审核通过走钱包 TASK_REWARD 入账。', 'TASK_CENTER', 'USDT', 2.500000, 0, 0, NULL, NULL, 2, 1, '', '', @seed_time, NULL),
('TASK-DEMO-003', '完成平台分享任务', '演示已完成任务，奖励流水已入账。', 'SHARE', 'USDT', 3.000000, 0, 0, NULL, NULL, 3, 1, '', '', @seed_time, NULL),
('TASK-DEMO-004', '提交社媒曝光证明', '演示被驳回任务，可重新提交。', 'SHARE', 'USDT', 4.000000, 1, 0, NULL, NULL, 4, 1, '', '', @seed_time, NULL),
('TASK-DEMO-005', 'VIP 专属任务', '未领取任务，用于可领取 tab 展示。', 'VIP', 'USDT', 5.000000, 2, 0, NULL, NULL, 5, 1, '', '', @seed_time, NULL)
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `description` = VALUES(`description`),
    `task_type` = VALUES(`task_type`),
    `currency` = VALUES(`currency`),
    `reward_amount` = VALUES(`reward_amount`),
    `required_vip_level` = VALUES(`required_vip_level`),
    `daily_limit` = VALUES(`daily_limit`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `update_time` = @seed_time;

INSERT INTO `mission_user_task`(`user_id`, `task_id`, `task_code`, `task_title`, `task_type`, `currency`, `reward_amount`, `status`, `submit_content`, `review_remark`, `wallet_flow_no`, `claimed_at`, `submitted_at`, `reviewed_at`, `finished_at`, `create_time`, `update_time`, `creator`, `updater`) VALUES
(178288000001, (SELECT `id` FROM `mission_task` WHERE `task_code` = 'TASK-DEMO-001' LIMIT 1), 'TASK-DEMO-001', '完成新手资料任务', 'GENERAL', 'USDT', 1.000000, 'CLAIMED', NULL, NULL, NULL, @seed_time, NULL, NULL, NULL, @seed_time, NULL, '', ''),
(178288000001, (SELECT `id` FROM `mission_task` WHERE `task_code` = 'TASK-DEMO-002' LIMIT 1), 'TASK-DEMO-002', '提交任务计划截图', 'TASK_CENTER', 'USDT', 2.500000, 'SUBMITTED', '已上传任务计划截图，等待审核。', NULL, NULL, @seed_time, @seed_time, NULL, NULL, @seed_time, NULL, '', ''),
(178288000001, (SELECT `id` FROM `mission_task` WHERE `task_code` = 'TASK-DEMO-003' LIMIT 1), 'TASK-DEMO-003', '完成平台分享任务', 'SHARE', 'USDT', 3.000000, 'APPROVED', '分享链接已提交。', '审核通过，奖励已入账。', 'FLOW-DEMO-TASK-001', @seed_time, @seed_time, @seed_time, @seed_time, @seed_time, NULL, '', ''),
(178288000001, (SELECT `id` FROM `mission_task` WHERE `task_code` = 'TASK-DEMO-004' LIMIT 1), 'TASK-DEMO-004', '提交社媒曝光证明', 'SHARE', 'USDT', 4.000000, 'REJECTED', '曝光截图不完整。', '截图不清晰，请重新提交。', NULL, @seed_time, @seed_time, @seed_time, @seed_time, @seed_time, NULL, '', ''),
(178288000003, (SELECT `id` FROM `mission_task` WHERE `task_code` = 'TASK-DEMO-002' LIMIT 1), 'TASK-DEMO-002', '提交任务计划截图', 'TASK_CENTER', 'USDT', 2.500000, 'APPROVED', 'VIP2 任务截图。', '审核通过，奖励已入账。', 'FLOW-DEMO-TASK-003', @seed_time, @seed_time, @seed_time, @seed_time, @seed_time, NULL, '', '')
ON DUPLICATE KEY UPDATE
    `task_code` = VALUES(`task_code`),
    `task_title` = VALUES(`task_title`),
    `task_type` = VALUES(`task_type`),
    `currency` = VALUES(`currency`),
    `reward_amount` = VALUES(`reward_amount`),
    `submit_content` = VALUES(`submit_content`),
    `review_remark` = VALUES(`review_remark`),
    `wallet_flow_no` = VALUES(`wallet_flow_no`),
    `claimed_at` = VALUES(`claimed_at`),
    `submitted_at` = VALUES(`submitted_at`),
    `reviewed_at` = VALUES(`reviewed_at`),
    `finished_at` = VALUES(`finished_at`),
    `update_time` = @seed_time;

INSERT INTO `mission_reward_settlement`(`user_task_id`, `user_id`, `task_id`, `currency`, `amount`, `biz_type`, `biz_id`, `status`, `wallet_flow_no`, `fail_reason`, `settled_at`, `create_time`, `update_time`, `creator`, `updater`) VALUES
((SELECT `id` FROM `mission_user_task` WHERE `user_id` = 178288000001 AND `task_code` = 'TASK-DEMO-003' AND `status` = 'APPROVED' LIMIT 1), 178288000001, (SELECT `id` FROM `mission_task` WHERE `task_code` = 'TASK-DEMO-003' LIMIT 1), 'USDT', 3.000000, 'TASK_REWARD', 'MUT-DEMO-APPROVED-001', 'SETTLED', 'FLOW-DEMO-TASK-001', NULL, @seed_time, @seed_time, NULL, '', ''),
((SELECT `id` FROM `mission_user_task` WHERE `user_id` = 178288000003 AND `task_code` = 'TASK-DEMO-002' AND `status` = 'APPROVED' LIMIT 1), 178288000003, (SELECT `id` FROM `mission_task` WHERE `task_code` = 'TASK-DEMO-002' LIMIT 1), 'USDT', 2.500000, 'TASK_REWARD', 'MUT-DEMO-APPROVED-003', 'SETTLED', 'FLOW-DEMO-TASK-003', NULL, @seed_time, @seed_time, NULL, '', '')
ON DUPLICATE KEY UPDATE
    `user_id` = VALUES(`user_id`),
    `task_id` = VALUES(`task_id`),
    `currency` = VALUES(`currency`),
    `amount` = VALUES(`amount`),
    `status` = VALUES(`status`),
    `wallet_flow_no` = VALUES(`wallet_flow_no`),
    `fail_reason` = VALUES(`fail_reason`),
    `settled_at` = VALUES(`settled_at`),
    `update_time` = @seed_time;

-- =============================================================
-- mall-promotion：抽奖配置和用户中奖记录
-- =============================================================
USE `mall-promotion`;

INSERT INTO `promotion_prize`(`id`, `prize_code`, `prize_name`, `prize_type`, `currency`, `amount`, `stock_total`, `stock_used`, `sort_order`, `status`, `remark`, `creator`, `updater`, `create_time`, `update_time`) VALUES
(1, 'LOTTERY_CASH_001', '现金奖励 0.100000 USDT', 'CASH', 'USDT', 0.100000, 0, 0, 1, 1, '现金奖中奖后通过钱包结算 Provider 入账', '', '', @seed_time, NULL),
(2, 'LOTTERY_CASH_002', '现金奖励 0.500000 USDT', 'CASH', 'USDT', 0.500000, 0, 0, 2, 1, '现金奖中奖后通过钱包结算 Provider 入账', '', '', @seed_time, NULL),
(3, 'LOTTERY_THANKS', '谢谢参与', 'POINT', 'USDT', 0.000000, 0, 0, 99, 1, '非现金奖，只记录中奖事实，不触发钱包入账', '', '', @seed_time, NULL)
ON DUPLICATE KEY UPDATE
    `prize_name` = VALUES(`prize_name`),
    `prize_type` = VALUES(`prize_type`),
    `currency` = VALUES(`currency`),
    `amount` = VALUES(`amount`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`),
    `update_time` = @seed_time;

INSERT INTO `promotion_lottery_activity`(`id`, `activity_code`, `title`, `description`, `daily_limit`, `start_at`, `end_at`, `sort_order`, `status`, `creator`, `updater`, `create_time`, `update_time`) VALUES
(1, 'DAILY_LOTTERY', '每日抽奖', '用户每日可参与的基础抽奖活动，中奖现金奖后写入可追踪抽奖记录并调用钱包结算入账。', 1, NULL, NULL, 1, 1, '', '', @seed_time, NULL)
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `description` = VALUES(`description`),
    `daily_limit` = VALUES(`daily_limit`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `update_time` = @seed_time;

INSERT INTO `promotion_lottery_prize`(`id`, `activity_id`, `prize_id`, `weight`, `daily_limit`, `sort_order`, `status`, `creator`, `updater`, `create_time`, `update_time`) VALUES
(1, 1, 1, 20, 0, 1, 1, '', '', @seed_time, NULL),
(2, 1, 2, 5, 0, 2, 1, '', '', @seed_time, NULL),
(3, 1, 3, 75, 0, 99, 1, '', '', @seed_time, NULL)
ON DUPLICATE KEY UPDATE
    `weight` = VALUES(`weight`),
    `daily_limit` = VALUES(`daily_limit`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `update_time` = @seed_time;

INSERT INTO `promotion_lottery_record`(`record_no`, `user_id`, `activity_id`, `activity_title`, `prize_id`, `prize_code`, `prize_name`, `prize_type`, `currency`, `amount`, `status`, `wallet_flow_no`, `fail_reason`, `drawn_at`, `settled_at`, `creator`, `updater`, `create_time`, `update_time`) VALUES
('LOTTERY-DEMO-001', 178288000001, 1, '每日抽奖', 2, 'LOTTERY_CASH_002', '现金奖励 0.500000 USDT', 'CASH', 'USDT', 0.500000, 'SETTLED', 'FLOW-DEMO-LOTTERY-001', NULL, @seed_time, @seed_time, '', '', @seed_time, NULL),
('LOTTERY-DEMO-002', 178288000002, 1, '每日抽奖', 3, 'LOTTERY_THANKS', '谢谢参与', 'POINT', 'USDT', 0.000000, 'DRAWN', NULL, NULL, @seed_time, NULL, '', '', @seed_time, NULL)
ON DUPLICATE KEY UPDATE
    `user_id` = VALUES(`user_id`),
    `activity_id` = VALUES(`activity_id`),
    `activity_title` = VALUES(`activity_title`),
    `prize_id` = VALUES(`prize_id`),
    `prize_code` = VALUES(`prize_code`),
    `prize_name` = VALUES(`prize_name`),
    `prize_type` = VALUES(`prize_type`),
    `currency` = VALUES(`currency`),
    `amount` = VALUES(`amount`),
    `status` = VALUES(`status`),
    `wallet_flow_no` = VALUES(`wallet_flow_no`),
    `fail_reason` = VALUES(`fail_reason`),
    `drawn_at` = VALUES(`drawn_at`),
    `settled_at` = VALUES(`settled_at`),
    `update_time` = @seed_time;