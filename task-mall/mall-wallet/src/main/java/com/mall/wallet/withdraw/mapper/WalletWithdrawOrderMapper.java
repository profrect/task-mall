package com.mall.wallet.withdraw.mapper;

import com.mall.wallet.withdraw.model.entity.WalletWithdrawOrder;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface WalletWithdrawOrderMapper extends BaseMapper<WalletWithdrawOrder> {

    @Select("SELECT COUNT(*) FROM wallet_withdraw_order WHERE coin = #{coin}")
    Long countByCoin(@Param("coin") String coin);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM wallet_withdraw_order " +
            "WHERE coin = #{coin} AND status IN ('APPROVED','BROADCASTING','CONFIRMED')")
    BigDecimal sumApprovedAmount(@Param("coin") String coin);

    @Select("SELECT COUNT(*) FROM wallet_withdraw_order WHERE coin = #{coin} AND status = 'REVIEWING'")
    Long countReviewing(@Param("coin") String coin);

    @Select("SELECT COUNT(*) FROM wallet_withdraw_order WHERE coin = #{coin} AND create_time >= #{startAt}")
    Long countToday(@Param("coin") String coin, @Param("startAt") Long startAt);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM wallet_withdraw_order " +
            "WHERE coin = #{coin} AND status IN ('APPROVED','BROADCASTING','CONFIRMED') AND create_time >= #{startAt}")
    BigDecimal sumTodayApprovedAmount(@Param("coin") String coin, @Param("startAt") Long startAt);
}