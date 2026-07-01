package com.mall.admin.controller.promotion;

import com.mall.admin.service.promotion.PromotionAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
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
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 抽奖后台接口：配置、奖池和记录都转发到 mall-promotion。 */
@RestController
@RequestMapping("/api/admin/promotion/lottery")
public class PromotionAdminController {

    @Resource
    private PromotionAdminService promotionAdminService;

    @GetMapping("/prizes/list")
    public Result<Page<PromotionPrizeResp>> prizePage(PromotionPrizeQueryReq req) throws BizException {
        return Result.ok(promotionAdminService.prizePage(req));
    }

    @PostMapping("/prizes")
    public Result<PromotionPrizeResp> savePrize(@RequestBody PromotionPrizeReq req) throws BizException {
        return Result.ok(promotionAdminService.savePrize(req));
    }

    @PutMapping("/prizes")
    public Result<PromotionPrizeResp> updatePrize(@RequestBody PromotionPrizeReq req) throws BizException {
        return Result.ok(promotionAdminService.savePrize(req));
    }

    @DeleteMapping("/prizes/{id}")
    public Result<Void> deletePrize(@PathVariable("id") @NotNull Long id) throws BizException {
        promotionAdminService.deletePrize(id);
        return Result.ok();
    }

    @GetMapping("/activities/list")
    public Result<Page<PromotionLotteryActivityResp>> activityPage(PromotionLotteryActivityQueryReq req)
            throws BizException {
        return Result.ok(promotionAdminService.activityPage(req));
    }

    @PostMapping("/activities")
    public Result<PromotionLotteryActivityResp> saveActivity(@RequestBody PromotionLotteryActivityReq req)
            throws BizException {
        return Result.ok(promotionAdminService.saveActivity(req));
    }

    @PutMapping("/activities")
    public Result<PromotionLotteryActivityResp> updateActivity(@RequestBody PromotionLotteryActivityReq req)
            throws BizException {
        return Result.ok(promotionAdminService.saveActivity(req));
    }

    @DeleteMapping("/activities/{id}")
    public Result<Void> deleteActivity(@PathVariable("id") @NotNull Long id) throws BizException {
        promotionAdminService.deleteActivity(id);
        return Result.ok();
    }

    @GetMapping("/activities/{activityId}/prizes")
    public Result<List<PromotionLotteryPrizeResp>> activityPrizes(@PathVariable("activityId") @NotNull Long activityId)
            throws BizException {
        return Result.ok(promotionAdminService.activityPrizes(activityId));
    }

    @PostMapping("/activity-prizes")
    public Result<PromotionLotteryPrizeResp> saveActivityPrize(@RequestBody PromotionLotteryPrizeReq req)
            throws BizException {
        return Result.ok(promotionAdminService.saveActivityPrize(req));
    }

    @PutMapping("/activity-prizes")
    public Result<PromotionLotteryPrizeResp> updateActivityPrize(@RequestBody PromotionLotteryPrizeReq req)
            throws BizException {
        return Result.ok(promotionAdminService.saveActivityPrize(req));
    }

    @DeleteMapping("/activity-prizes/{id}")
    public Result<Void> deleteActivityPrize(@PathVariable("id") @NotNull Long id) throws BizException {
        promotionAdminService.deleteActivityPrize(id);
        return Result.ok();
    }

    @GetMapping("/records/list")
    public Result<Page<PromotionLotteryRecordResp>> recordPage(PromotionLotteryRecordQueryReq req)
            throws BizException {
        return Result.ok(promotionAdminService.recordPage(req));
    }
}