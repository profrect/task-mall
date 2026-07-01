package com.mall.wallet.mapper;

import com.mall.common.model.dto.resp.LeaderboardItemResp;
import com.mall.wallet.model.entity.WalletFlowDetail;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WalletFlowDetailMapper extends BaseMapper<WalletFlowDetail> {

    @Select("""
            <script>
            SELECT user_id AS userId,
                   SUM(change_amt) AS metricValue,
                   COUNT(*) AS recordCount,
                   MAX(create_time) AS lastEventTime
            FROM wallet_flow_detail
            WHERE direction = 'IN'
              AND biz_type IN
              <foreach collection="bizTypes" item="bizType" open="(" separator="," close=")">
                  #{bizType}
              </foreach>
              <if test="startTime != null">
                  AND create_time &gt;= #{startTime}
              </if>
              <if test="endTime != null">
                  AND create_time &lt;= #{endTime}
              </if>
            GROUP BY user_id
            ORDER BY metricValue DESC, lastEventTime ASC, user_id ASC
            LIMIT #{limit}
            </script>
            """)
    List<LeaderboardItemResp> leaderboardByBizTypes(@Param("bizTypes") List<String> bizTypes,
                                                     @Param("startTime") Long startTime,
                                                     @Param("endTime") Long endTime,
                                                     @Param("limit") int limit);
}