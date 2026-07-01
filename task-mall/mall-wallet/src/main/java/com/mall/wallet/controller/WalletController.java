package com.mall.wallet.controller;

import com.mall.common.auth.util.AuthUtils;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.resp.WalletFlowResp;
import com.mall.wallet.chain.ChainCode;
import com.mall.wallet.chain.address.DepositAddressService;
import com.mall.wallet.enums.WalletRespCodeEnum;
import com.mall.wallet.model.entity.WalletAccount;
import com.mall.wallet.model.entity.WalletFlowDetail;
import com.mall.wallet.model.vo.WalletAccountVO;
import com.mall.wallet.recharge.RechargeService;
import com.mall.wallet.recharge.model.entity.WalletRechargeOrder;
import com.mall.wallet.recharge.model.vo.DepositAddressVO;
import com.mall.wallet.recharge.model.vo.RechargeOrderVO;
import com.mall.wallet.service.WalletAccountService;
import com.mall.wallet.service.WalletFlowQueryService;
import com.mall.wallet.transfer.TransferService;
import com.mall.wallet.transfer.model.dto.TransferApplyDTO;
import com.mall.wallet.transfer.model.entity.WalletTransferOrder;
import com.mall.wallet.transfer.model.vo.TransferOrderVO;
import com.mall.wallet.withdraw.WithdrawService;
import com.mall.wallet.withdraw.model.dto.WithdrawApplyDTO;
import com.mall.wallet.withdraw.model.entity.WalletWithdrawOrder;
import com.mall.wallet.withdraw.model.vo.WithdrawOrderVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

/**
 * 钱包用户侧接口。
 * <p>
 * userId 一律从 sa-token 解析，绝不接受前端传入，从根上杜绝越权读他人钱包。
 * 首次访问即懒初始化账户（getOrCreate），账户开通与账务变动解耦。
 */
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Resource
    private WalletAccountService walletAccountService;

    @Resource
    private WalletFlowQueryService walletFlowQueryService;

    @Resource
    private DepositAddressService depositAddressService;

    @Resource
    private RechargeService rechargeService;

    @Resource
    private WithdrawService withdrawService;

    @Resource
    private TransferService transferService;

    @GetMapping("/overview")
    public Result<WalletAccountVO> overview() throws BizException {
        Long userId = AuthUtils.currentUserId();
        WalletAccount account = walletAccountService.getOrCreate(userId, null);
        return Result.ok(toVO(account));
    }

    /** 本人账务流水（按时间倒序），只读展示账务内核 append 的资金变更事实。 */
    @GetMapping("/flows")
    public Result<List<WalletFlowResp>> flows(@RequestParam(value = "limit", defaultValue = "200") Integer limit)
            throws BizException {
        Long userId = AuthUtils.currentUserId();
        List<WalletFlowResp> records = walletFlowQueryService.listUserFlows(userId, limit == null ? 200 : limit)
                .stream()
                .map(this::toFlowResp)
                .toList();
        return Result.ok(records);
    }

    /**
     * 取本人在指定链的充值收款地址，没有则即时分配（每用户每链唯一）。
     * 阶段1默认 TRON；返回值绝不含私钥。
     */
    @GetMapping("/deposit/address")
    public Result<DepositAddressVO> depositAddress(
            @RequestParam(value = "chain", defaultValue = "TRON") String chain) throws BizException {
        Long userId = AuthUtils.currentUserId();
        ChainCode chainCode = parseChain(chain);
        // 仅当该链充值扫块已启用(chain_config.enabled=1)才发放收款地址：杜绝发出无人扫块入账的地址、
        // 导致用户充值后资金无法到账。EVM 链须先配置 rpc-url 并把 chain_config.enabled 置 1 才开放。
        if (rechargeService.getEnabledChainConfig(chainCode) == null) {
            throw new BizException(WalletRespCodeEnum.DEPOSIT_ADDRESS_UNAVAILABLE,
                    new Object[]{chainCode.name() + " 充值暂未开放"});
        }
        String address = depositAddressService.getOrAssign(userId, chainCode);
        return Result.ok(DepositAddressVO.builder()
                .chainCode(chainCode.name())
                .address(address)
                .build());
    }

    /**
     * 本人充值记录（按时间倒序）。映射为 VO，隐藏内部审计列。
     */
    @GetMapping("/recharge/records")
    public Result<List<RechargeOrderVO>> rechargeRecords() throws BizException {
        Long userId = AuthUtils.currentUserId();
        List<RechargeOrderVO> records = rechargeService.listUserRecharges(userId).stream()
                .map(this::toVO)
                .toList();
        return Result.ok(records);
    }

    /**
     * 申请提现：冻结可用余额并落单，进入人工审核。chain 默认 TRON、coin 默认 USDT；userId 由会话解析。
     */
    @PostMapping("/withdraw/apply")
    public Result<WithdrawOrderVO> withdrawApply(@RequestBody WithdrawApplyDTO dto) throws BizException {
        Long userId = AuthUtils.currentUserId();
        ChainCode chainCode = parseChain(dto.getChain());
        WalletWithdrawOrder order = withdrawService.apply(
                userId, chainCode, dto.getCoin(), dto.getToAddress(), dto.getAmount());
        return Result.ok(toVO(order));
    }

    /**
     * 本人提现记录（按时间倒序）。映射为 VO，隐藏审核人身份与内部审计列。
     */
    @GetMapping("/withdraw/records")
    public Result<List<WithdrawOrderVO>> withdrawRecords() throws BizException {
        Long userId = AuthUtils.currentUserId();
        List<WithdrawOrderVO> records = withdrawService.listUserWithdraws(userId).stream()
                .map(this::toVO)
                .toList();
        return Result.ok(records);
    }

    /**
     * 站内转账：转出与转入在同一事务内完成，并写双方账务流水。
     */
    @PostMapping("/transfer/apply")
    public Result<TransferOrderVO> transferApply(@RequestBody TransferApplyDTO dto) throws BizException {
        Long userId = AuthUtils.currentUserId();
        WalletTransferOrder order = transferService.apply(
                userId, dto.getToUserId(), dto.getCoin(), dto.getAmount(), dto.getRemark());
        return Result.ok(toVO(order));
    }

    /** 本人站内转账记录（转出/转入均展示，按时间倒序）。 */
    @GetMapping("/transfer/records")
    public Result<List<TransferOrderVO>> transferRecords() throws BizException {
        Long userId = AuthUtils.currentUserId();
        return Result.ok(transferService.listForUser(userId).stream()
                .map(this::toVO)
                .toList());
    }

    private ChainCode parseChain(String chain) throws BizException {
        try {
            return ChainCode.valueOf(chain.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BizException(WalletRespCodeEnum.CHAIN_NOT_SUPPORTED, new Object[]{chain});
        }
    }

    private WalletAccountVO toVO(WalletAccount a) {
        return WalletAccountVO.builder()
                .userId(a.getUserId())
                .currency(a.getCurrency())
                .totalBalance(a.getTotalBalance())
                .availBalance(a.getAvailBalance())
                .frozenBalance(a.getFrozenBalance())
                .build();
    }

    private WalletFlowResp toFlowResp(WalletFlowDetail f) {
        WalletFlowResp r = new WalletFlowResp();
        r.setFlowNo(f.getFlowNo());
        r.setUserId(f.getUserId());
        r.setBizType(f.getBizType());
        r.setBizId(f.getBizId());
        r.setDirection(f.getDirection());
        r.setChangeAmt(f.getChangeAmt());
        r.setBalanceBefore(f.getBalanceBefore());
        r.setBalanceAfter(f.getBalanceAfter());
        r.setRemark(f.getRemark());
        r.setCreateTime(f.getCreateTime());
        return r;
    }

    private RechargeOrderVO toVO(WalletRechargeOrder o) {
        return RechargeOrderVO.builder()
                .orderNo(o.getOrderNo())
                .chainCode(o.getChainCode())
                .coin(o.getCoin())
                .amount(o.getAmount())
                .fromAddress(o.getFromAddress())
                .toAddress(o.getToAddress())
                .txHash(o.getTxHash())
                .confirmations(o.getConfirmations())
                .status(o.getStatus())
                .creditedAt(o.getCreditedAt())
                .createTime(o.getCreateTime())
                .build();
    }

    private TransferOrderVO toVO(WalletTransferOrder o) {
        return TransferOrderVO.builder()
                .orderNo(o.getOrderNo())
                .fromUserId(o.getFromUserId())
                .toUserId(o.getToUserId())
                .coin(o.getCoin())
                .amount(o.getAmount())
                .status(o.getStatus())
                .remark(o.getRemark())
                .finishedAt(o.getFinishedAt())
                .createTime(o.getCreateTime())
                .build();
    }

    private WithdrawOrderVO toVO(WalletWithdrawOrder o) {
        return WithdrawOrderVO.builder()
                .orderNo(o.getOrderNo())
                .chainCode(o.getChainCode())
                .coin(o.getCoin())
                .amount(o.getAmount())
                .fee(o.getFee())
                .receiveAmount(o.getReceiveAmount())
                .toAddress(o.getToAddress())
                .txHash(o.getTxHash())
                .confirmations(o.getConfirmations())
                .status(o.getStatus())
                .reviewRemark(o.getReviewRemark())
                .reviewedAt(o.getReviewedAt())
                .broadcastAt(o.getBroadcastAt())
                .finishedAt(o.getFinishedAt())
                .createTime(o.getCreateTime())
                .build();
    }
}