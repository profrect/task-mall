package com.mall.wallet.recharge.mapper;

import com.mall.wallet.recharge.model.entity.WalletRechargeOrder;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface WalletRechargeOrderMapper extends BaseMapper<WalletRechargeOrder> {

    @Select("SELECT COUNT(*) FROM wallet_recharge_order WHERE coin = #{coin}")
    Long countByCoin(@Param("coin") String coin);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM wallet_recharge_order WHERE coin = #{coin} AND status = 'CREDITED'")
    BigDecimal sumCreditedAmount(@Param("coin") String coin);

    @Select("SELECT COUNT(*) FROM wallet_recharge_order WHERE coin = #{coin} AND create_time >= #{startAt}")
    Long countToday(@Param("coin") String coin, @Param("startAt") Long startAt);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM wallet_recharge_order " +
            "WHERE coin = #{coin} AND status = 'CREDITED' AND create_time >= #{startAt}")
    BigDecimal sumTodayCreditedAmount(@Param("coin") String coin, @Param("startAt") Long startAt);
}