package com.mall.wallet.mapper;

import com.mall.wallet.model.entity.WalletAccount;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface WalletAccountMapper extends BaseMapper<WalletAccount> {

    /**
     * 以行锁(SELECT ... FOR UPDATE)读取账户，串行化同一 (用户, 币种) 的并发资金操作。
     * 必须在事务中调用，否则锁会立即释放。
     */
    @Select("SELECT * FROM wallet_account WHERE user_id = #{userId} AND currency = #{currency} FOR UPDATE")
    WalletAccount selectForUpdate(@Param("userId") Long userId, @Param("currency") String currency);

    @Select("SELECT COUNT(*) FROM wallet_account WHERE currency = #{currency}")
    Long countByCurrency(@Param("currency") String currency);

    @Select("SELECT COALESCE(SUM(total_balance), 0) FROM wallet_account WHERE currency = #{currency}")
    BigDecimal sumTotalBalance(@Param("currency") String currency);

    @Select("SELECT COALESCE(SUM(avail_balance), 0) FROM wallet_account WHERE currency = #{currency}")
    BigDecimal sumAvailBalance(@Param("currency") String currency);

    @Select("SELECT COALESCE(SUM(frozen_balance), 0) FROM wallet_account WHERE currency = #{currency}")
    BigDecimal sumFrozenBalance(@Param("currency") String currency);
}