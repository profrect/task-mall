package com.mall.wallet.transfer.mapper;

import com.mall.wallet.transfer.model.entity.WalletTransferOrder;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WalletTransferOrderMapper extends BaseMapper<WalletTransferOrder> {

    @Select("""
            SELECT id,
                   order_no AS orderNo,
                   from_user_id AS fromUserId,
                   to_user_id AS toUserId,
                   coin,
                   amount,
                   status,
                   remark,
                   finished_at AS finishedAt,
                   create_time AS createTime,
                   update_time AS updateTime,
                   creator,
                   updater
            FROM wallet_transfer_order
            WHERE from_user_id = #{userId} OR to_user_id = #{userId}
            ORDER BY id DESC
            """)
    List<WalletTransferOrder> listByUser(@Param("userId") Long userId);

    @Select("""
            SELECT id,
                   order_no AS orderNo,
                   from_user_id AS fromUserId,
                   to_user_id AS toUserId,
                   coin,
                   amount,
                   status,
                   remark,
                   finished_at AS finishedAt,
                   create_time AS createTime,
                   update_time AS updateTime,
                   creator,
                   updater
            FROM wallet_transfer_order
            WHERE from_user_id = #{userId} OR to_user_id = #{userId}
            ORDER BY id DESC
            LIMIT #{limit}
            """)
    List<WalletTransferOrder> listForAdminByUser(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("""
            SELECT id,
                   order_no AS orderNo,
                   from_user_id AS fromUserId,
                   to_user_id AS toUserId,
                   coin,
                   amount,
                   status,
                   remark,
                   finished_at AS finishedAt,
                   create_time AS createTime,
                   update_time AS updateTime,
                   creator,
                   updater
            FROM wallet_transfer_order
            ORDER BY id DESC
            LIMIT #{limit}
            """)
    List<WalletTransferOrder> listForAdmin(@Param("limit") int limit);
}