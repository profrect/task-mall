-- liquibase formatted sql
-- changeset GM:wallet_init_data_20260629001

-- 支付审计 seed：仅写非 PAID 状态，作为通道审计页面可复现数据。
-- 若插入 PAID/已入账状态，必须同步存在对应业务订单与钱包流水，不能由 payment_order 直接代表余额变化。
INSERT INTO `payment_order`(`order_no`, `user_id`, `business_type`, `business_order_no`, `channel_code`, `channel_order_no`, `currency`, `amount`, `status`, `pay_address`, `payer_address`, `tx_hash`, `audit_remark`, `paid_at`, `expired_at`, `creator`, `updater`, `create_time`, `update_time`)
VALUES ('PAY-AUDIT-DEMO-RECHARGE-PENDING', 178288000001, 'RECHARGE', NULL, 'TRON', 'TRON-PENDING-DEMO-001', 'USDT', 50.000000, 'PENDING', 'TTaskMallAuditPending111111111111', NULL, NULL, '测试充值支付意图，尚未观测到链上入账，不改变钱包余额。', NULL, 1782966400000, '', '', 1782880000000, NULL),
       ('PAY-AUDIT-DEMO-VIP-CLOSED', 178288000001, 'VIP_UPGRADE', 'VIP-DEMO-CLOSED-001', 'MANUAL', 'MANUAL-CLOSED-DEMO-001', 'USDT', 100.000000, 'CLOSED', 'manual-counter', NULL, NULL, '测试VIP支付审计单已关闭，未扣款、未升级。', NULL, 1782966400000, '', '', 1782880000000, NULL),
       ('PAY-AUDIT-DEMO-OTHER-FAILED', 178288000002, 'OTHER', 'OTHER-DEMO-FAILED-001', 'TRON', 'TRON-FAILED-DEMO-001', 'USDT', 20.000000, 'FAILED', 'TTaskMallAuditFailed1111111111111', 'TTaskMallPayerFailed111111111111', '0xpay_audit_failed_demo', '测试失败支付审计单，仅用于通道对账观察。', NULL, 1782966400000, '', '', 1782880000000, NULL)
ON DUPLICATE KEY UPDATE `business_type` = VALUES(`business_type`),
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
                        `expired_at` = VALUES(`expired_at`),
                        `update_time` = VALUES(`create_time`);