package com.mall.admin.service.mission;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.MissionGoodsQueryReq;
import com.mall.common.model.dto.req.MissionGoodsReq;
import com.mall.common.model.dto.req.MissionTaskQueryReq;
import com.mall.common.model.dto.req.MissionTaskReq;
import com.mall.common.model.dto.req.MissionTaskReviewReq;
import com.mall.common.model.dto.req.MissionUserTaskQueryReq;
import com.mall.common.model.dto.resp.MissionGoodsResp;
import com.mall.common.model.dto.resp.MissionTaskResp;
import com.mall.common.model.dto.resp.MissionUserTaskResp;
import com.mybatisflex.core.paginate.Page;

public interface MissionAdminService {

    Page<MissionGoodsResp> goodsPage(MissionGoodsQueryReq req) throws BizException;

    MissionGoodsResp saveGoods(MissionGoodsReq req) throws BizException;

    void deleteGoods(Long id) throws BizException;

    Page<MissionTaskResp> taskPage(MissionTaskQueryReq req) throws BizException;

    MissionTaskResp saveTask(MissionTaskReq req) throws BizException;

    void deleteTask(Long id) throws BizException;

    Page<MissionUserTaskResp> recordPage(MissionUserTaskQueryReq req) throws BizException;

    MissionUserTaskResp approve(MissionTaskReviewReq req) throws BizException;

    MissionUserTaskResp reject(MissionTaskReviewReq req) throws BizException;
}