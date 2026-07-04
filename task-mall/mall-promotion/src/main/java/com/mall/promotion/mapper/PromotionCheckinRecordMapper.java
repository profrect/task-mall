package com.mall.promotion.mapper;

import com.mall.promotion.model.entity.PromotionCheckinRecord;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PromotionCheckinRecordMapper extends BaseMapper<PromotionCheckinRecord> {

    @Select("""
            SELECT *
            FROM promotion_checkin_record
            WHERE user_id = #{userId}
              AND checkin_date = #{checkinDate}
            LIMIT 1
            """)
    PromotionCheckinRecord selectByUserAndDate(@Param("userId") Long userId, @Param("checkinDate") Integer checkinDate);

    @Select("""
            SELECT *
            FROM promotion_checkin_record
            WHERE user_id = #{userId}
              AND checkin_date < #{checkinDate}
            ORDER BY checkin_date DESC
            LIMIT 1
            """)
    PromotionCheckinRecord selectLatestBefore(@Param("userId") Long userId, @Param("checkinDate") Integer checkinDate);
}