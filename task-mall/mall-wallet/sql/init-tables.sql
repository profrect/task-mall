-- =============================================================
-- mall-wallet 钱包服务 schema（手工执行；本项目 Liquibase 默认 enabled:false）
--
-- 资金精度：decimal(36,6)。对 USDT(TRC20/ERC20/Polygon 均 6 位小数) 无损存储；
--          BEP20-USDT 为 18 位小数，入账时按 coin_config.decimals 归一化到 6 位（阶段2处理）。
-- 审计字段(create_time/update_time/creator/updater) 由 MyBatis-Flex 监听器回填，
--          无登录上下文(如扫块定时任务)时 creator 留空，create_time 仍按系统时钟写入。
-- 金额方向语义：change_amt 恒为正，资金增减由 direction(IN/OUT/FREEZE/UNFREEZE) 表达。
-- =============================================================

-- 钱包账户主表：每用户每币种一条；账务内核(applyLedger)唯一可改余额的对象。
-- 不变式：total_balance = avail_balance + frozen_balance；三者恒 >= 0。
CREATE TABLE `wallet_account`
(
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`        bigint         NOT NULL COMMENT '全局唯一用户ID',
    `currency`       varchar(16)    NOT NULL DEFAULT 'USDT' COMMENT '钱包币种',
    `total_balance`  decimal(36, 6) NOT NULL DEFAULT 0 COMMENT '总余额 = 可用 + 冻结',
    `avail_balance`  decimal(36, 6) NOT NULL DEFAULT 0 COMMENT '可用余额',
    `frozen_balance` decimal(36, 6) NOT NULL DEFAULT 0 COMMENT '冻结余额',
    `create_time`    bigint                  DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time`    bigint                  DEFAULT NULL COMMENT '数据修改时间（UTC毫秒）',
    `creator`        varchar(50)             DEFAULT '' COMMENT '数据创建者',
    `updater`        varchar(50)             DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_currency` (`user_id`, `currency`)
) ENGINE = InnoDB COMMENT ='钱包账户主表';

-- 钱包流水明细：每次余额变动追加一条，作为账变审计与对账依据。
-- 幂等底线：同一业务(biz_type + biz_id) 全局只允许入账一次，由 uk_biz 唯一约束兜底。
--          充值场景 biz_id = 链上 txHash(同笔多转账时拼接 logIndex)，故需足够长度。
CREATE TABLE `wallet_flow_detail`
(
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `flow_no`        varchar(64)    NOT NULL COMMENT '流水号（业务可读唯一编号）',
    `user_id`        bigint         NOT NULL COMMENT '全局唯一用户ID',
    `wallet_id`      bigint         NOT NULL COMMENT '钱包账户记录id(wallet_account.id)',
    `biz_type`       varchar(32)    NOT NULL COMMENT '业务类型: RECHARGE/WITHDRAW/TRANSFER/...',
    `biz_id`         varchar(128)   NOT NULL COMMENT '业务幂等键: 充值=txHash[:logIndex]，提现/转账=订单号',
    `direction`      varchar(8)     NOT NULL COMMENT '资金方向: IN/OUT/FREEZE/UNFREEZE',
    `change_amt`     decimal(36, 6) NOT NULL DEFAULT 0 COMMENT '变动金额(恒为正)',
    `balance_before` decimal(36, 6) NOT NULL DEFAULT 0 COMMENT '变动前可用余额快照',
    `balance_after`  decimal(36, 6) NOT NULL DEFAULT 0 COMMENT '变动后可用余额快照',
    `remark`         varchar(200)            DEFAULT NULL COMMENT '描述',
    `create_time`    bigint                  DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time`    bigint                  DEFAULT NULL COMMENT '数据修改时间（UTC毫秒）',
    `creator`        varchar(50)             DEFAULT '' COMMENT '数据创建者',
    `updater`        varchar(50)             DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_flow_no` (`flow_no`),
    UNIQUE KEY `uk_biz` (`biz_type`, `biz_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_wallet` (`wallet_id`)
) ENGINE = InnoDB COMMENT ='钱包流水明细';

-- 充值订单：用户侧可见的充值记录。在扫块检测到转入本人充值地址时创建，
--          确认数达标后由账务内核入账并置为 CREDITED。
-- 幂等：uk_tx(chain_code, tx_hash, log_index) 保证一笔链上转账只生成一条充值订单。
CREATE TABLE `wallet_recharge_order`
(
    `id`            bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_no`      varchar(64)    NOT NULL COMMENT '充值订单号(内部唯一)',
    `user_id`       bigint         NOT NULL COMMENT '全局唯一用户ID',
    `chain_code`    varchar(16)    NOT NULL COMMENT '链: TRON/ETH/BSC/POLYGON',
    `coin`          varchar(16)    NOT NULL DEFAULT 'USDT' COMMENT '币种',
    `amount`        decimal(36, 6) NOT NULL DEFAULT 0 COMMENT '到账金额(归一化后)',
    `from_address`  varchar(64)             DEFAULT NULL COMMENT '付款地址',
    `to_address`    varchar(64)    NOT NULL COMMENT '收款地址(本人充值地址)',
    `tx_hash`       varchar(80)    NOT NULL COMMENT '链上交易哈希',
    `log_index`     int            NOT NULL DEFAULT 0 COMMENT '同笔交易内转账日志序号',
    `confirmations` int            NOT NULL DEFAULT 0 COMMENT '当前确认数',
    `status`        varchar(16)    NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/CONFIRMING/CREDITED/EXPIRED',
    `credited_at`   bigint                  DEFAULT NULL COMMENT '入账完成时间（UTC毫秒）',
    `create_time`   bigint                  DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time`   bigint                  DEFAULT NULL COMMENT '数据修改时间（UTC毫秒）',
    `creator`       varchar(50)             DEFAULT '' COMMENT '数据创建者',
    `updater`       varchar(50)             DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    UNIQUE KEY `uk_tx` (`chain_code`, `tx_hash`, `log_index`),
    KEY `idx_user` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB COMMENT ='充值订单';

-- 支付订单审计：记录通道/外部支付观察事实，只读稽核，不直接改钱包余额。
-- 资金边界：支付成功若要影响余额，必须由充值/业务订单状态机调用 wallet_account 账务内核完成；
--          本表只用于通道单号、链上哈希、业务单号之间的对账追踪。
CREATE TABLE `payment_order`
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

-- =============================================================
-- 链上对接数据模型（多链适配的持久层；链差异封装在适配层，业务编排不感知具体链）
-- =============================================================

-- 链运行配置：控制启用哪些链、入账确认门槛。
-- 安全约定：rpc_ref / explorer_ref 只存“配置键名”，真实节点 URL 与第三方 API Key
--          一律走 yml/环境变量解析，绝不入库、不进 git。
CREATE TABLE `chain_config`
(
    `id`                bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `chain_code`        varchar(16) NOT NULL COMMENT '链编码: TRON/ETH/BSC/POLYGON',
    `chain_id`          bigint               DEFAULT NULL COMMENT 'EVM chainId（TRON 无，留空）',
    `rpc_ref`           varchar(64)          DEFAULT NULL COMMENT 'RPC 配置键名（指向 yml/env，不存真实地址/密钥）',
    `explorer_ref`      varchar(64)          DEFAULT NULL COMMENT '浏览器/第三方API 配置键名（同上）',
    `min_confirmations` int         NOT NULL DEFAULT 1 COMMENT '入账所需最小确认数',
    `enabled`           tinyint     NOT NULL DEFAULT 0 COMMENT '是否启用扫块（0-否，1-是）',
    `create_time`       bigint               DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time`       bigint               DEFAULT NULL COMMENT '数据修改时间（UTC毫秒）',
    `creator`           varchar(50)          DEFAULT '' COMMENT '数据创建者',
    `updater`           varchar(50)          DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chain_code` (`chain_code`)
) ENGINE = InnoDB COMMENT ='链运行配置';

-- 币种配置：每(链,符号)的合约地址与链上精度。
-- USDT 精度：TRC20/ERC20/Polygon = 6 位；BSC(BEP20) = 18 位（入账按此归一化到账务标度 6 位）。
CREATE TABLE `coin_config`
(
    `id`               bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `chain_code`       varchar(16) NOT NULL COMMENT '链编码',
    `symbol`           varchar(16) NOT NULL DEFAULT 'USDT' COMMENT '币种符号',
    `contract_address` varchar(64)          DEFAULT NULL COMMENT '代币合约地址（原生币留空）',
    `decimals`         int         NOT NULL DEFAULT 6 COMMENT '链上精度（小数位）',
    `enabled`          tinyint     NOT NULL DEFAULT 1 COMMENT '是否启用（0-否，1-是）',
    `create_time`      bigint               DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time`      bigint               DEFAULT NULL COMMENT '数据修改时间（UTC毫秒）',
    `creator`          varchar(50)          DEFAULT '' COMMENT '数据创建者',
    `updater`          varchar(50)          DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chain_symbol` (`chain_code`, `symbol`)
) ENGINE = InnoDB COMMENT ='币种配置';

-- 用户充值地址：每用户每链一个收款地址（trident 即时生成密钥对）。
-- 安全约定（阶段1）：私钥不以明文存储，仅以 AES-GCM 加密后存入 enc_priv_key；主密钥走配置/环境变量，不入库不进 git。
--                  阶段2迁移 KMS 托管后，本列改存 KMS 密文引用，业务代码（DepositAddressService/SecretCipher 边界）不变。
-- uk_chain_addr 支持扫块时 (chain, to_address) 反查归属用户。
CREATE TABLE `user_deposit_address`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`       bigint       NOT NULL COMMENT '全局唯一用户ID',
    `chain_code`    varchar(16)  NOT NULL COMMENT '链编码',
    `address`       varchar(64)  NOT NULL COMMENT '充值收款地址',
    `enc_priv_key`  varchar(512) NOT NULL COMMENT '加密私钥（AES-GCM, base64；阶段2迁移KMS密文引用）',
    `create_time`   bigint                DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time`   bigint                DEFAULT NULL COMMENT '数据修改时间（UTC毫秒）',
    `creator`       varchar(50)           DEFAULT '' COMMENT '数据创建者',
    `updater`       varchar(50)           DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_chain` (`user_id`, `chain_code`),
    UNIQUE KEY `uk_chain_addr` (`chain_code`, `address`)
) ENGINE = InnoDB COMMENT ='用户充值地址';

-- 链上充值事件：扫块写入的“链上观测事实”（基础设施层），与用户侧充值订单分层。
-- 幂等键 uk_event(chain_code, tx_hash, log_index)：一笔链上转账只记一行。
-- amount_raw 为链上原始整数单位（字符串，不丢精度）；归一化到账务标度在生成充值订单时按 coin_config.decimals 处理。
-- status: SEEN(已观测) -> CONFIRMED(确认达标) -> CREDITED(已入账)；IGNORED(非充值地址/无关转账)。
CREATE TABLE `chain_deposit_event`
(
    `id`               bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `chain_code`       varchar(16) NOT NULL COMMENT '链编码',
    `contract_address` varchar(64)          DEFAULT NULL COMMENT '代币合约地址（观测到的 token）',
    `tx_hash`          varchar(80) NOT NULL COMMENT '链上交易哈希',
    `log_index`        int         NOT NULL DEFAULT 0 COMMENT '同笔交易内转账日志序号',
    `from_address`     varchar(64)          DEFAULT NULL COMMENT '付款地址',
    `to_address`       varchar(64) NOT NULL COMMENT '收款地址',
    `amount_raw`       varchar(80) NOT NULL COMMENT '链上原始整数金额（未按 decimals 归一化）',
    `block_height`     bigint               DEFAULT NULL COMMENT '所在区块高度',
    `confirmations`    int         NOT NULL DEFAULT 0 COMMENT '当前确认数',
    `status`           varchar(16) NOT NULL DEFAULT 'SEEN' COMMENT '状态: SEEN/CONFIRMED/CREDITED/IGNORED',
    `create_time`      bigint               DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time`      bigint               DEFAULT NULL COMMENT '数据修改时间（UTC毫秒）',
    `creator`          varchar(50)          DEFAULT '' COMMENT '数据创建者',
    `updater`          varchar(50)          DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_event` (`chain_code`, `tx_hash`, `log_index`),
    KEY `idx_to_addr` (`chain_code`, `to_address`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB COMMENT ='链上充值事件';

-- =============================================================
-- 提现出款（支付二期）：热钱包出款 + 人工审核 + 三阶段账务
-- =============================================================

-- 提现订单：用户侧可见的提现记录，同时是提现状态机的载体（business 层）。
-- 账务幂等：order_no 作为 wallet_flow_detail.biz_id；提现三阶段(WITHDRAW_FREEZE/SETTLE/UNFREEZE)
--          biz_type 不同、共享同一 order_no，互不幂等覆盖（账务内核保证不重复冻结/结算/解冻）。
-- 状态机：REVIEWING→APPROVED→BROADCASTING→CONFIRMED(终态)；REVIEWING→REJECTED(终态)；APPROVED/BROADCASTING→FAILED(终态)。
-- 资金语义：amount=申请总额(冻结/结算均以此为准)；fee=手续费(平台留存)；receive_amount=链上实际转出=amount-fee。
CREATE TABLE `wallet_withdraw_order`
(
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_no`       varchar(64)    NOT NULL COMMENT '提现订单号(内部唯一，账务幂等键)',
    `user_id`        bigint         NOT NULL COMMENT '全局唯一用户ID',
    `chain_code`     varchar(16)    NOT NULL COMMENT '链: TRON/ETH/BSC/POLYGON',
    `coin`           varchar(16)    NOT NULL DEFAULT 'USDT' COMMENT '币种',
    `amount`         decimal(36, 6) NOT NULL COMMENT '申请提现总额(冻结/结算以此为准)',
    `fee`            decimal(36, 6) NOT NULL DEFAULT 0 COMMENT '手续费(平台留存)',
    `receive_amount` decimal(36, 6) NOT NULL COMMENT '实际到账(链上转出) = amount - fee',
    `to_address`     varchar(64)    NOT NULL COMMENT '提现目标地址',
    `from_address`   varchar(64)             DEFAULT NULL COMMENT '出款热钱包地址(广播时回填)',
    `tx_hash`        varchar(80)             DEFAULT NULL COMMENT '链上交易哈希(广播后回填)',
    `confirmations`  int            NOT NULL DEFAULT 0 COMMENT '当前确认数',
    `status`         varchar(16)    NOT NULL DEFAULT 'REVIEWING' COMMENT '状态: REVIEWING/APPROVED/BROADCASTING/CONFIRMED/REJECTED/FAILED',
    `reviewer`       varchar(50)             DEFAULT NULL COMMENT '审核管理员',
    `review_remark`  varchar(200)            DEFAULT NULL COMMENT '审核备注/失败原因',
    `reviewed_at`    bigint                  DEFAULT NULL COMMENT '审核时间（UTC毫秒）',
    `broadcast_at`   bigint                  DEFAULT NULL COMMENT '广播时间（UTC毫秒）',
    `finished_at`    bigint                  DEFAULT NULL COMMENT '终态时间：确认/驳回/失败（UTC毫秒）',
    `create_time`    bigint                  DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time`    bigint                  DEFAULT NULL COMMENT '数据修改时间（UTC毫秒）',
    `creator`        varchar(50)             DEFAULT '' COMMENT '数据创建者',
    `updater`        varchar(50)             DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_tx` (`tx_hash`)
) ENGINE = InnoDB COMMENT ='提现订单';

-- 站内转账订单：用户之间的真实资金划转记录。
-- 资金语义：成功订单必须对应 wallet_flow_detail 中同 order_no 的 TRANSFER_OUT 与 TRANSFER_IN 两条流水；
--          订单本身只记录业务事实，不直接替代账务流水。
CREATE TABLE `wallet_transfer_order`
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

-- =============================================================
-- 归集（sweep）：把分散在各用户充值地址上的 USDT 集中到出款热钱包
-- =============================================================

-- 归集订单：一次「用户充值地址 → 出款热钱包」的资金搬运任务，承载归集状态机。
-- 资金语义：归集是平台内部资金搬运，用户余额在充值入账时已确定，故归集全程【不触发账务】
--          （不调 applyLedger、不写 wallet_flow_detail）。
--          amount=创建时余额快照；swept_amount=归集广播时刻实时全部余额(回填)。
-- 状态机：CREATED →(native不足则 GAS_FUNDING →)→ SWEEPING → COMPLETED(终态)；任一步失败→FAILED(终态)。
-- 幂等：归集转实时全余额(为0则跳过) + 喂gas前查native(够则跳过) + 状态CAS(WHERE status=旧态)；
--      「同地址至多一个非终态订单」由编排层创建前校验保证，避免并发重复归集。
CREATE TABLE `collect_order`
(
    `id`            bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_no`      varchar(64)    NOT NULL COMMENT '归集订单号(内部唯一)',
    `user_id`       bigint         NOT NULL COMMENT '归属用户ID',
    `chain_code`    varchar(16)    NOT NULL COMMENT '链: TRON/ETH/BSC/POLYGON',
    `coin`          varchar(16)    NOT NULL DEFAULT 'USDT' COMMENT '币种',
    `from_address`  varchar(64)    NOT NULL COMMENT '归集来源(用户充值地址)',
    `to_address`    varchar(64)             DEFAULT NULL COMMENT '归集目标(出款热钱包地址，归集广播时回填)',
    `amount`        decimal(36, 6) NOT NULL DEFAULT 0 COMMENT '创建时USDT余额快照(阈值决策/展示)',
    `swept_amount`  decimal(36, 6)          DEFAULT NULL COMMENT '实际归集转出额(广播时刻实时全余额，回填)',
    `gas_tx_hash`   varchar(80)             DEFAULT NULL COMMENT 'gas代付交易哈希(需喂gas时回填)',
    `sweep_tx_hash` varchar(80)             DEFAULT NULL COMMENT '归集交易哈希(归集广播后回填)',
    `confirmations` int            NOT NULL DEFAULT 0 COMMENT '归集交易当前确认数',
    `status`        varchar(16)    NOT NULL DEFAULT 'CREATED' COMMENT '状态: CREATED/GAS_FUNDING/SWEEPING/COMPLETED/FAILED',
    `retry_count`   int            NOT NULL DEFAULT 0 COMMENT '链上动作重试计数(达上限置FAILED)',
    `remark`        varchar(512)            DEFAULT NULL COMMENT '失败原因/备注',
    `gas_sent_at`   bigint                  DEFAULT NULL COMMENT 'gas代付广播时间(UTC毫秒)',
    `swept_at`      bigint                  DEFAULT NULL COMMENT '归集广播时间(UTC毫秒)',
    `finished_at`   bigint                  DEFAULT NULL COMMENT '终态时间:完成/失败(UTC毫秒)',
    `create_time`   bigint                  DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time`   bigint                  DEFAULT NULL COMMENT '数据修改时间（UTC毫秒）',
    `creator`       varchar(50)             DEFAULT '' COMMENT '数据创建者',
    `updater`       varchar(50)             DEFAULT '' COMMENT '数据修改者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_chain_addr` (`chain_code`, `from_address`),
    KEY `idx_status` (`status`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB COMMENT ='归集订单';

-- =============================================================
-- 基础配置 seed（幂等：依赖唯一键，已存在则忽略，绝不覆盖运营手工调整）
--
-- chain_config.enabled：控制「充值扫块 + 充值地址发放」是否启用。
--   TRON=1：充值扫块已上线；
--   ETH/BSC/POLYGON=0：EVM 充值扫块(eth_getLogs lookback 窗口)逻辑已落地，但默认关闭——
--                      须先配好 wallet.evm.chains.*.rpc-url，再 UPDATE 置 enabled=1，才会扫块并发放收款地址
--                      （未启用时 /deposit/address 直接拒发，避免发出无人入账的地址导致充值丢币）。
--   注意：提现出款不依赖本开关，由 wallet.withdraw.chains.* 与热钱包配置共同决定。
-- coin_config.enabled=1：出款解析合约地址与链上精度、入账归一化的真相源，全部启用。
-- USDT 链上精度：TRON/ETH/Polygon = 6 位；BSC(BEP20) = 18 位。
-- 合约地址为各链主网官方 USDT；私链/测试网请按实覆盖。
-- =============================================================
INSERT IGNORE INTO `chain_config` (`chain_code`, `chain_id`, `rpc_ref`, `explorer_ref`, `min_confirmations`, `enabled`)
VALUES ('TRON', NULL, 'wallet.tron.grpc-endpoint', 'wallet.tron.endpoint', 19, 1),
       ('ETH', 1, 'wallet.evm.chains.ETH.rpc-url', NULL, 12, 0),
       ('BSC', 56, 'wallet.evm.chains.BSC.rpc-url', NULL, 15, 0),
       ('POLYGON', 137, 'wallet.evm.chains.POLYGON.rpc-url', NULL, 128, 0);

INSERT IGNORE INTO `coin_config` (`chain_code`, `symbol`, `contract_address`, `decimals`, `enabled`)
VALUES ('TRON', 'USDT', 'TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t', 6, 1),
       ('ETH', 'USDT', '0xdAC17F958D2ee523a2206206994597C13D831ec7', 6, 1),
       ('BSC', 'USDT', '0x55d398326f99059fF775485246999027B3197955', 18, 1),
       ('POLYGON', 'USDT', '0xc2132D05D31c914a87C6611C10748AEb04B58e8F', 6, 1);

-- =============================================================
-- 支付审计 seed：仅写非 PAID 状态，作为通道审计页面可复现数据。
-- 若插入 PAID/已入账状态，必须同步存在对应业务订单与钱包流水，不能由 payment_order 直接代表余额变化。
-- =============================================================
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