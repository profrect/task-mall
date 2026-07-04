package com.mall.promotion.mapper;

import com.mall.promotion.model.entity.PromotionCouponRecord;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PromotionCouponRecordMapper extends BaseMapper<PromotionCouponRecord> {

    @Select("""
            SELECT COUNT(*)
            FROM promotion_coupon_record
            WHERE user_id = #{userId}
              AND template_id = #{templateId}
            """)
    Long countClaimedByTemplate(@Param("userId") Long userId, @Param("templateId") Long templateId);
}