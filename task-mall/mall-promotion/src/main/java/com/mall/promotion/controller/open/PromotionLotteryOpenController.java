package com.mall.promotion.controller.open;

import com.mall.common.auth.util.AuthUtils;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.resp.PromotionLotteryDetailResp;
import com.mall.common.model.dto.resp.PromotionLotteryRecordResp;
import com.mall.promotion.service.PromotionLotteryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 移动端抽奖接口：本人身份只取登录态，不接受前端传 userId。 */
@RestController
@RequestMapping("/api/open/promotion/lottery")
public class PromotionLotteryOpenController {

    @Resource
    private PromotionLotteryService promotionLotteryService;

    @GetMapping("/activities")
    public Result<List<PromotionLotteryDetailResp>> activities() throws BizException {
        return Result.ok(promotionLotteryService.openActivities(AuthUtils.currentUserId()));
    }

    @PostMapping("/activities/{activityId}/draw")
    public Result<PromotionLotteryRecordResp> draw(@PathVariable("activityId") Long activityId) throws BizException {
        return Result.ok(promotionLotteryService.draw(AuthUtils.currentUserId(), activityId));
    }

    @GetMapping("/records")
    public Result<List<PromotionLotteryRecordResp>> records(@RequestParam(required = false) Integer limit)
            throws BizException {
        return Result.ok(promotionLotteryService.userRecords(AuthUtils.currentUserId(), limit));
    }
}