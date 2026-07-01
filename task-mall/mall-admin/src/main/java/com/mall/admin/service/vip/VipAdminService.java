package com.mall.admin.service.vip;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.VipLevelConfigReq;
import com.mall.common.model.dto.resp.VipLevelConfigResp;

import java.util.List;

/** VIP 配置管理聚合：配置事实由 mall-user 持有，admin 只做运维入口。 */
public interface VipAdminService {

    List<VipLevelConfigResp> list(Integer status) throws BizException;

    VipLevelConfigResp save(VipLevelConfigReq req) throws BizException;

    void delete(Long id) throws BizException;
}