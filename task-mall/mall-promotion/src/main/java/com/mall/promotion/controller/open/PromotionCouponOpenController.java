package com.mall.promotion.controller.open;

import com.mall.common.auth.util.AuthUtils;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.promotion.model.vo.PromotionCheckinRecordVO;
import com.mall.promotion.model.vo.PromotionCheckinStateVO;
import com.mall.promotion.model.vo.PromotionCouponRecordVO;
import com.mall.promotion.model.vo.PromotionCouponTemplateVO;
import com.mall.promotion.service.PromotionCouponService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 移动端优惠券/签到接口：本人身份只取登录态，不接受前端传 userId。 */
@RestController
@RequestMapping("/api/open/promotion")
public class PromotionCouponOpenController {

    @Resource
    private PromotionCouponService promotionCouponService;

    @GetMapping("/coupon/templates")
    public Result<List<PromotionCouponTemplateVO>> couponTemplates(@RequestParam(name = "limit", required = false) Integer limit)
            throws BizException {
        return Result.ok(promotionCouponService.openTemplates(AuthUtils.currentUserId(), limit));
    }

    @PostMapping("/coupon/templates/{templateId}/claim")
    public Result<PromotionCouponRecordVO> claimCoupon(@PathVariable("templateId") Long templateId) throws BizException {
        AuthUtils.ensureNotImpersonated();
        return Result.ok(promotionCouponService.claim(AuthUtils.currentUserId(), templateId));
    }

    @GetMapping("/coupon/records")
    public Result<List<PromotionCouponRecordVO>> couponRecords(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "limit", required = false) Integer limit) throws BizException {
        return Result.ok(promotionCouponService.userCoupons(AuthUtils.currentUserId(), status, limit));
    }

    @GetMapping("/checkin/state")
    public Result<PromotionCheckinStateVO> checkinState(@RequestParam(name = "limit", required = false) Integer limit)
            throws BizException {
        return Result.ok(promotionCouponService.checkinState(AuthUtils.currentUserId(), limit));
    }

    @PostMapping("/checkin")
    public Result<PromotionCheckinRecordVO> checkin() throws BizException {
        AuthUtils.ensureNotImpersonated();
        return Result.ok(promotionCouponService.checkin(AuthUtils.currentUserId()));
    }

    @GetMapping("/checkin/records")
    public Result<List<PromotionCheckinRecordVO>> checkinRecords(@RequestParam(name = "limit", required = false) Integer limit)
            throws BizException {
        return Result.ok(promotionCouponService.checkinRecords(AuthUtils.currentUserId(), limit));
    }
}