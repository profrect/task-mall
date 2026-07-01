package com.mall.promotion.mapper;

import com.mall.promotion.model.entity.PromotionLotteryRecord;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PromotionLotteryRecordMapper extends BaseMapper<PromotionLotteryRecord> {

    @Select("""
            SELECT COUNT(*)
            FROM promotion_lottery_record
            WHERE user_id = #{userId}
              AND activity_id = #{activityId}
              AND drawn_at >= #{dayStart}
            """)
    Long countToday(@Param("userId") Long userId,
                    @Param("activityId") Long activityId,
                    @Param("dayStart") Long dayStart);

    @Select("""
            SELECT COUNT(*)
            FROM promotion_lottery_record
            WHERE prize_id = #{prizeId}
              AND drawn_at >= #{dayStart}
            """)
    Long countPrizeToday(@Param("prizeId") Long prizeId, @Param("dayStart") Long dayStart);
}