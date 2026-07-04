package com.mall.promotion.service;

import com.mall.common.core.exception.BizException;
import com.mall.promotion.model.vo.PromotionCheckinRecordVO;
import com.mall.promotion.model.vo.PromotionCheckinStateVO;
import com.mall.promotion.model.vo.PromotionCouponRecordVO;
import com.mall.promotion.model.vo.PromotionCouponTemplateVO;

import java.util.List;

public interface PromotionCouponService {

    List<PromotionCouponTemplateVO> openTemplates(Long userId, Integer limit) throws BizException;

    PromotionCouponRecordVO claim(Long userId, Long templateId) throws BizException;

    List<PromotionCouponRecordVO> userCoupons(Long userId, String status, Integer limit) throws BizException;

    PromotionCheckinStateVO checkinState(Long userId, Integer limit) throws BizException;

    PromotionCheckinRecordVO checkin(Long userId) throws BizException;

    List<PromotionCheckinRecordVO> checkinRecords(Long userId, Integer limit) throws BizException;
}