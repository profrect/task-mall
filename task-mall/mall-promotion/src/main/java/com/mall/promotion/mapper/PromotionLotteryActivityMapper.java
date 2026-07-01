package com.mall.promotion.mapper;

import com.mall.promotion.model.entity.PromotionLotteryActivity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PromotionLotteryActivityMapper extends BaseMapper<PromotionLotteryActivity> {

    @Select("""
            SELECT id,
                   activity_code AS activityCode,
                   title,
                   description,
                   daily_limit AS dailyLimit,
                   start_at AS startAt,
                   end_at AS endAt,
                   sort_order AS sortOrder,
                   status,
                   creator,
                   updater,
                   create_time AS createTime,
                   update_time AS updateTime
            FROM promotion_lottery_activity
            WHERE status = 1
              AND (start_at IS NULL OR start_at <= #{now})
              AND (end_at IS NULL OR end_at >= #{now})
            ORDER BY sort_order ASC, id DESC
            """)
    List<PromotionLotteryActivity> listOpenActivities(@Param("now") Long now);
}