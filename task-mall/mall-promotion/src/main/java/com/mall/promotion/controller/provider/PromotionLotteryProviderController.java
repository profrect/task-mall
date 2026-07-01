package com.mall.promotion.controller.provider;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.IdReq;
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
import com.mall.promotion.service.PromotionLotteryService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 后台聚合 provider：促销事实只由 mall-promotion 持有。 */
@RestController
@RequestMapping("/api/provider/promotion/lottery")
public class PromotionLotteryProviderController {

    @Resource
    private PromotionLotteryService promotionLotteryService;

    @PostMapping("/prizes/page")
    public Result<Page<PromotionPrizeResp>> prizePage(@RequestBody PromotionPrizeQueryReq req) throws BizException {
        return Result.ok(promotionLotteryService.prizePage(req));
    }

    @PostMapping("/prizes/save")
    public Result<PromotionPrizeResp> savePrize(@RequestBody PromotionPrizeReq req) throws BizException {
        return Result.ok(promotionLotteryService.savePrize(req));
    }

    @PostMapping("/prizes/delete")
    public Result<Void> deletePrize(@RequestBody IdReq req) throws BizException {
        promotionLotteryService.deletePrize(req.getId());
        return Result.ok();
    }

    @PostMapping("/activities/page")
    public Result<Page<PromotionLotteryActivityResp>> activityPage(@RequestBody PromotionLotteryActivityQueryReq req)
            throws BizException {
        return Result.ok(promotionLotteryService.activityPage(req));
    }

    @PostMapping("/activities/save")
    public Result<PromotionLotteryActivityResp> saveActivity(@RequestBody PromotionLotteryActivityReq req)
            throws BizException {
        return Result.ok(promotionLotteryService.saveActivity(req));
    }

    @PostMapping("/activities/delete")
    public Result<Void> deleteActivity(@RequestBody IdReq req) throws BizException {
        promotionLotteryService.deleteActivity(req.getId());
        return Result.ok();
    }

    @PostMapping("/activity-prizes/list")
    public Result<List<PromotionLotteryPrizeResp>> activityPrizes(@RequestBody IdReq req) throws BizException {
        return Result.ok(promotionLotteryService.activityPrizes(req.getId()));
    }

    @PostMapping("/activity-prizes/save")
    public Result<PromotionLotteryPrizeResp> saveActivityPrize(@RequestBody PromotionLotteryPrizeReq req)
            throws BizException {
        return Result.ok(promotionLotteryService.saveActivityPrize(req));
    }

    @PostMapping("/activity-prizes/delete")
    public Result<Void> deleteActivityPrize(@RequestBody IdReq req) throws BizException {
        promotionLotteryService.deleteActivityPrize(req.getId());
        return Result.ok();
    }

    @PostMapping("/records/page")
    public Result<Page<PromotionLotteryRecordResp>> recordPage(@RequestBody PromotionLotteryRecordQueryReq req)
            throws BizException {
        return Result.ok(promotionLotteryService.recordPage(req));
    }
}