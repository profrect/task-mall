package com.mall.mission.mapper;

import com.mall.common.model.dto.resp.LeaderboardItemResp;
import com.mall.mission.model.entity.MissionUserTask;
import com.mall.mission.model.vo.MissionTaskVO;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface MissionUserTaskMapper extends BaseMapper<MissionUserTask> {

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM mission_user_task
            WHERE user_id = #{userId}
              AND status = #{status}
              <if test="taskType != null and taskType != ''">
                  AND task_type = #{taskType}
              </if>
            </script>
            """)
    Long countByStatus(@Param("userId") Long userId,
                       @Param("status") String status,
                       @Param("taskType") String taskType);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM mission_user_task
            WHERE user_id = #{userId}
              AND status IN ('CLAIMED', 'SUBMITTED', 'REJECTED')
              <if test="taskType != null and taskType != ''">
                  AND task_type = #{taskType}
              </if>
            </script>
            """)
    Long countInProgress(@Param("userId") Long userId, @Param("taskType") String taskType);

    @Select("""
            <script>
            SELECT COALESCE(SUM(reward_amount), 0)
            FROM mission_user_task
            WHERE user_id = #{userId}
              AND status = 'APPROVED'
              <if test="taskType != null and taskType != ''">
                  AND task_type = #{taskType}
              </if>
            </script>
            """)
    BigDecimal sumApprovedReward(@Param("userId") Long userId, @Param("taskType") String taskType);

    @Select("""
            <script>
            SELECT user_id AS userId,
                   COUNT(*) AS metricValue,
                   COUNT(*) AS recordCount,
                   MAX(finished_at) AS lastEventTime
            FROM mission_user_task
            WHERE status = 'APPROVED'
              <if test="startTime != null">
                  AND finished_at &gt;= #{startTime}
              </if>
              <if test="endTime != null">
                  AND finished_at &lt;= #{endTime}
              </if>
            GROUP BY user_id
            ORDER BY metricValue DESC, SUM(reward_amount) DESC, lastEventTime ASC, user_id ASC
            LIMIT #{limit}
            </script>
            """)
    List<LeaderboardItemResp> taskLeaderboard(@Param("startTime") Long startTime,
                                              @Param("endTime") Long endTime,
                                              @Param("limit") int limit);

    @Select("""
            <script>
            SELECT t.id AS taskId,
                   t.id AS id,
                   NULL AS recordId,
                   t.task_code AS taskCode,
                   t.title,
                   t.description,
                   t.task_type AS taskType,
                   t.currency,
                   t.reward_amount AS rewardAmount,
                   t.required_vip_level AS requiredVipLevel,
                   NULL AS userStatus,
                   NULL AS submitContent,
                   NULL AS reviewRemark,
                   NULL AS claimedAt,
                   NULL AS submittedAt,
                   NULL AS reviewedAt
            FROM mission_task t
            WHERE t.status = 1
              AND (t.start_at IS NULL OR t.start_at &lt;= #{now})
              AND (t.end_at IS NULL OR t.end_at &gt;= #{now})
              <if test="taskType != null and taskType != ''">
                  AND t.task_type = #{taskType}
              </if>
              AND NOT EXISTS (
                  SELECT 1
                  FROM mission_user_task ut
                  WHERE ut.user_id = #{userId}
                    AND ut.task_id = t.id
                    AND ut.status IN ('CLAIMED', 'SUBMITTED', 'APPROVED', 'REJECTED')
              )
            ORDER BY t.sort_order ASC, t.id DESC
            LIMIT #{limit}
            </script>
            """)
    List<MissionTaskVO> listAvailable(@Param("userId") Long userId,
                                      @Param("now") Long now,
                                      @Param("taskType") String taskType,
                                      @Param("limit") int limit);

    @Select("""
            <script>
            SELECT ut.task_id AS taskId,
                   ut.task_id AS id,
                   ut.id AS recordId,
                   ut.task_code AS taskCode,
                   ut.task_title AS title,
                   t.description,
                   ut.task_type AS taskType,
                   ut.currency,
                   ut.reward_amount AS rewardAmount,
                   t.required_vip_level AS requiredVipLevel,
                   ut.status AS userStatus,
                   ut.submit_content AS submitContent,
                   ut.review_remark AS reviewRemark,
                   ut.claimed_at AS claimedAt,
                   ut.submitted_at AS submittedAt,
                   ut.reviewed_at AS reviewedAt
            FROM mission_user_task ut
            LEFT JOIN mission_task t ON t.id = ut.task_id
            WHERE ut.user_id = #{userId}
              AND ut.status IN ('CLAIMED', 'SUBMITTED', 'REJECTED')
              <if test="taskType != null and taskType != ''">
                  AND ut.task_type = #{taskType}
              </if>
            ORDER BY ut.id DESC
            LIMIT #{limit}
            </script>
            """)
    List<MissionTaskVO> listInProgress(@Param("userId") Long userId,
                                       @Param("taskType") String taskType,
                                       @Param("limit") int limit);

    @Select("""
            <script>
            SELECT ut.task_id AS taskId,
                   ut.task_id AS id,
                   ut.id AS recordId,
                   ut.task_code AS taskCode,
                   ut.task_title AS title,
                   t.description,
                   ut.task_type AS taskType,
                   ut.currency,
                   ut.reward_amount AS rewardAmount,
                   t.required_vip_level AS requiredVipLevel,
                   ut.status AS userStatus,
                   ut.submit_content AS submitContent,
                   ut.review_remark AS reviewRemark,
                   ut.claimed_at AS claimedAt,
                   ut.submitted_at AS submittedAt,
                   ut.reviewed_at AS reviewedAt
            FROM mission_user_task ut
            LEFT JOIN mission_task t ON t.id = ut.task_id
            WHERE ut.user_id = #{userId}
              AND ut.status IN ('APPROVED', 'CANCELLED', 'EXPIRED')
              <if test="taskType != null and taskType != ''">
                  AND ut.task_type = #{taskType}
              </if>
            ORDER BY ut.id DESC
            LIMIT #{limit}
            </script>
            """)
    List<MissionTaskVO> listCompleted(@Param("userId") Long userId,
                                      @Param("taskType") String taskType,
                                      @Param("limit") int limit);
}