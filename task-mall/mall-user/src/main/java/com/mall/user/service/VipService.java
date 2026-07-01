package com.mall.user.service;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.VipLevelConfigReq;
import com.mall.common.model.dto.resp.VipLevelConfigResp;
import com.mall.user.model.dto.VipLevelUpDTO;
import com.mall.user.model.vo.VipLevelOverviewVO;
import com.mall.user.model.vo.VipUpgradeOrderVO;

import java.util.List;

/** VIP 配置与升级域：配置归用户域，资金扣款统一走钱包结算 provider。 */
public interface VipService {

    VipLevelOverviewVO userOverview(long userId) throws BizException;

    VipUpgradeOrderVO upgrade(long userId, VipLevelUpDTO dto) throws BizException;

    List<VipLevelConfigResp> configList(Integer status) throws BizException;

    VipLevelConfigResp saveConfig(VipLevelConfigReq req) throws BizException;

    void deleteConfig(Long id) throws BizException;
}