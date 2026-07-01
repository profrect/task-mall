package com.mall.promotion.mapper;

import com.mall.common.model.dto.resp.PromotionLotteryPrizeResp;
import com.mall.promotion.model.entity.PromotionLotteryPrize;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PromotionLotteryPrizeMapper extends BaseMapper<PromotionLotteryPrize> {

    @Select("""
            SELECT lp.id,
                   lp.activity_id AS activityId,
                   lp.prize_id AS prizeId,
                   p.prize_code AS prizeCode,
                   p.prize_name AS prizeName,
                   p.prize_type AS prizeType,
                   p.currency,
                   p.amount,
                   lp.weight,
                   lp.daily_limit AS dailyLimit,
                   lp.sort_order AS sortOrder,
                   lp.status,
                   lp.create_time AS createTime,
                   lp.update_time AS updateTime
            FROM promotion_lottery_prize lp
            JOIN promotion_prize p ON p.id = lp.prize_id
            WHERE lp.activity_id = #{activityId}
            ORDER BY lp.sort_order ASC, lp.id DESC
            """)
    List<PromotionLotteryPrizeResp> listByActivity(@Param("activityId") Long activityId);

    @Select("""
            SELECT lp.id,
                   lp.activity_id AS activityId,
                   lp.prize_id AS prizeId,
                   p.prize_code AS prizeCode,
                   p.prize_name AS prizeName,
                   p.prize_type AS prizeType,
                   p.currency,
                   p.amount,
                   lp.weight,
                   lp.daily_limit AS dailyLimit,
                   lp.sort_order AS sortOrder,
                   lp.status,
                   lp.create_time AS createTime,
                   lp.update_time AS updateTime
            FROM promotion_lottery_prize lp
            JOIN promotion_prize p ON p.id = lp.prize_id
            WHERE lp.activity_id = #{activityId}
              AND lp.status = 1
              AND p.status = 1
              AND (p.stock_total = 0 OR p.stock_used < p.stock_total)
            ORDER BY lp.sort_order ASC, lp.id DESC
            """)
    List<PromotionLotteryPrizeResp> listDrawableByActivity(@Param("activityId") Long activityId);
}