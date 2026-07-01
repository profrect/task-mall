package com.mall.wallet.collect.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 归集订单：把某用户充值地址上的 USDT 集中到出款热钱包的一次任务，承载归集状态机。
 * <p>
 * <strong>资金语义</strong>：归集是平台内部资金搬运（用户充值地址 → 出款热钱包）。用户余额在充值入账时
 * 已记账，故归集全程<strong>不触发账务</strong>（不调 applyLedger、不写 wallet_flow_detail）。
 * <ul>
 *   <li>amount：创建订单时的 USDT 余额快照，用于阈值决策与展示；</li>
 *   <li>sweptAmount：实际归集转出额 = 归集广播时刻的实时全部余额（广播后回填，可能 != amount）。</li>
 * </ul>
 * 幂等：同一 (chainCode, fromAddress) 同时只允许一个进行中订单（status 非终态），创建前校验兜底。
 */
@Data
@Table(value = "collect_order", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class CollectOrder extends BaseEntity<Long> {

    private String orderNo;

    private Long userId;

    private String chainCode;

    private String coin;

    /** 归集来源：用户充值地址。 */
    private String fromAddress;

    /** 归集目标：出款热钱包地址（发起归集广播时回填）。 */
    private String toAddress;

    /** 创建时 USDT 余额快照（阈值决策 / 展示用）。 */
    private BigDecimal amount;

    /** 实际归集转出额 = 归集广播时刻实时全部余额（归集广播后回填）。 */
    private BigDecimal sweptAmount;

    /** gas 代付交易哈希（需先喂 gas 时回填）。 */
    private String gasTxHash;

    /** 归集交易哈希（归集广播后回填）。 */
    private String sweepTxHash;

    /** 归集交易当前确认数。 */
    private Integer confirmations;

    private String status;

    /** 链上动作重试计数（广播失败累计，达上限置 FAILED）。 */
    private Integer retryCount;

    /** 失败原因 / 备注。 */
    private String remark;

    /** gas 代付广播时间（UTC 毫秒）。 */
    private Long gasSentAt;

    /** 归集广播时间（UTC 毫秒）。 */
    private Long sweptAt;

    /** 终态时间：完成 / 失败（UTC 毫秒）。 */
    private Long finishedAt;
}