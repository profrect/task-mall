package com.mall.wallet.collect.mapper;

import com.mall.wallet.collect.model.entity.CollectOrder;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface CollectOrderMapper extends BaseMapper<CollectOrder> {

    @Select("SELECT COUNT(*) FROM collect_order WHERE coin = #{coin}")
    Long countByCoin(@Param("coin") String coin);

    @Select("SELECT COALESCE(SUM(swept_amount), 0) FROM collect_order WHERE coin = #{coin} AND status = 'COMPLETED'")
    BigDecimal sumCompletedAmount(@Param("coin") String coin);

    @Select("SELECT COUNT(*) FROM collect_order WHERE coin = #{coin} AND status IN ('CREATED','GAS_FUNDING','SWEEPING')")
    Long countActive(@Param("coin") String coin);

    @Select("SELECT COUNT(*) FROM collect_order WHERE coin = #{coin} AND create_time >= #{startAt}")
    Long countToday(@Param("coin") String coin, @Param("startAt") Long startAt);

    @Select("SELECT COALESCE(SUM(swept_amount), 0) FROM collect_order " +
            "WHERE coin = #{coin} AND status = 'COMPLETED' AND create_time >= #{startAt}")
    BigDecimal sumTodayCompletedAmount(@Param("coin") String coin, @Param("startAt") Long startAt);
}