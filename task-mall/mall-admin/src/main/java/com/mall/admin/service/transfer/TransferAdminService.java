package com.mall.admin.service.transfer;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.resp.WalletTransferOrderResp;

import java.util.List;

/** 站内转账订单查询（管理端）：转发 mall-wallet provider，只读展示。 */
public interface TransferAdminService {

    List<WalletTransferOrderResp> list(Long userId, Integer limit) throws BizException;
}