package com.mall.admin.service.wallet;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.resp.WalletFlowResp;

import java.util.List;

/** 账务流水查询（管理端）：转发 mall-wallet provider，只读审计资金变更事实。 */
public interface WalletFlowAdminService {

    /** 全站流水：userId 可选，最新在前，limit 限量。 */
    List<WalletFlowResp> list(Long userId, Integer limit) throws BizException;
}