package com.mall.wallet.chain.adapter;

import com.mall.common.core.exception.BizException;
import com.mall.wallet.chain.ChainCode;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

/**
 * 链适配器：每条链只需实现这一组能力，业务编排（充值/提现/归集）与具体链彻底解耦。
 * <p>
 * 设计边界：
 * - 充值地址的产生方式由各适配器自行决定（阶段1 TRON = trident 即时生成密钥对，Option D）；
 *   若适配器生成了私钥，则通过 {@link KeyMaterial} 带出，由地址分配层加密落库（业务库不存明文私钥）。
 * - 出款（transferOut）涉及私钥签名广播，私钥由签名边界（PayoutService）即时解密后通过
 *   {@link PayoutCommand} 传入，适配器用后即弃；同一能力服务提现与归集。
 */
public interface ChainAdapter {

    /**
     * 该适配器服务的链。注册表据此装配 {@code ChainCode -> ChainAdapter}。
     */
    ChainCode chainCode();

    /**
     * 产生一个新的收款地址。
     * <p>
     * 阶段1（Option D）：即时生成密钥对，返回 {@code address + privateKeyHex}；私钥由调用方加密后落库。
     * 返回的私钥严禁明文持久化或写入日志。
     */
    KeyMaterial newDepositAddress() throws BizException;

    /**
     * 扫描给定监听地址集合上的新 USDT 转入，返回观测事件（含确认数）。
     * 幂等去重由编排层按 (chain, txHash, logIndex) 唯一约束处理，适配器只负责"拉取最近转入"。
     */
    List<DepositEvent> scanDeposits(Collection<String> watchAddresses) throws BizException;

    /**
     * 出款：用指定私钥签名一笔代币转账并广播，返回交易哈希。
     * <p>
     * 同时服务「提现出款」（from = 热钱包）与「归集」（from = 用户充值地址）——
     * 两者本质都是"用某地址私钥把代币转给目标地址并广播"。私钥仅在内存流转，适配器用后即弃。
     */
    String transferOut(PayoutCommand command) throws BizException;

    /**
     * 查询交易当前确认数（用于提现广播后的确认轮询）。交易尚未上链 / 不存在返回 0。
     */
    long confirmations(String txHash) throws BizException;

    // ============================================================
    // 归集（sweep）专用能力：把用户充值地址上的 USDT 集中到出款热钱包。
    // 充值地址是「随机生成密钥对」且无 gas，故归集需先判断余额、必要时代付 gas，再转出代币。
    // ============================================================

    /**
     * 查询某地址的代币(USDT)余额，返回\u003cstrong\u003e链上原始整数\u003c/strong\u003e（未按 decimals 归一化）。
     * 归集前据此判断该地址是否值得归集（高于阈值才动）。只读调用，不涉及私钥。
     */
    BigInteger tokenBalanceRaw(String address, String contractAddress) throws BizException;

    /**
     * 查询某地址的原生币(gas)余额，返回\u003cstrong\u003e链上最小单位整数\u003c/strong\u003e（EVM=wei，TRON=sun）。
     * 归集前据此判断是否需先代付 gas（用户充值地址通常无原生币，无法自付转账手续费）。只读调用。
     */
    BigInteger nativeBalanceRaw(String address) throws BizException;

    /**
     * 转出原生币（gas 代付）：用 gas 供给地址私钥签名一笔\u003cstrong\u003e原生币\u003c/strong\u003e转账并广播，返回交易哈希。
     * \u003cp\u003e
     * 与 {@link #transferOut} 对称，但转的是原生币（EVM 的 value 字段 / TRON 的 TransferContract）而非代币：
     * 复用 {@link PayoutCommand}，仅取 from/privateKey/to/amount；contractAddress 留空、decimals 由各适配器
     * 按本链原生币精度内部决定（EVM=18，TRON=6）。私钥仅内存流转，用后即弃。
     */
    String transferNative(PayoutCommand command) throws BizException;

    /**
     * 本链原生币(gas 代币)的精度小数位（EVM=18 即 wei，TRON=6 即 sun）。
     * <p>
     * 精度是链的固有属性，归属适配层。归集编排据此在「人类可读的 gas 阈值 / 代付额」与
     * {@link #nativeBalanceRaw} 返回的最小单位整数之间换算，避免把链精度知识泄漏到编排层。
     */
    int nativeDecimals();
}