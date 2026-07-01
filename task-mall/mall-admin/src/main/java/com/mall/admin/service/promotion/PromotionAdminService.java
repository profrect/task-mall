package com.mall.admin.service.promotion;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.PromotionLotteryActivityQueryReq;
import com.mall.common.model.dto.req.PromotionLotteryActivityReq;
import com.mall.common.model.dto.req.PromotionLotteryPrizeReq;
import com.mall.common.model.dto.req.PromotionLotteryRecordQueryReq;
import com.mall.common.model.dto.req.PromotionPrizeQueryReq;
import com.mall.common.model.dto.req.PromotionPrizeReq;
import com.mall.common.model.dto.resp.PromotionLotteryActivityResp;
import com.mall.common.model.dto.resp.PromotionLotteryPrizeResp;
import com.mall.common.model.dto.resp.PromotionLotteryRecordResp;
import com.mall.common.model.dto.resp.PromotionPrizeResp;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

public interface PromotionAdminService {

    Page<PromotionPrizeResp> prizePage(PromotionPrizeQueryReq req) throws BizException;

    PromotionPrizeResp savePrize(PromotionPrizeReq req) throws BizException;

    void deletePrize(Long id) throws BizException;

    Page<PromotionLotteryActivityResp> activityPage(PromotionLotteryActivityQueryReq req) throws BizException;

    PromotionLotteryActivityResp saveActivity(PromotionLotteryActivityReq req) throws BizException;

    void deleteActivity(Long id) throws BizException;

    List<PromotionLotteryPrizeResp> activityPrizes(Long activityId) throws BizException;

    PromotionLotteryPrizeResp saveActivityPrize(PromotionLotteryPrizeReq req) throws BizException;

    void deleteActivityPrize(Long id) throws BizException;

    Page<PromotionLotteryRecordResp> recordPage(PromotionLotteryRecordQueryReq req) throws BizException;
}