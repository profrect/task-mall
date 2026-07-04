package com.mall.mission.service;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.MissionGoodsQueryReq;
import com.mall.common.model.dto.req.MissionGoodsReq;
import com.mall.common.model.dto.req.MissionTaskQueryReq;
import com.mall.common.model.dto.req.MissionTaskReq;
import com.mall.common.model.dto.req.MissionTaskReviewReq;
import com.mall.common.model.dto.req.MissionUserTaskQueryReq;
import com.mall.common.model.dto.resp.LeaderboardItemResp;
import com.mall.common.model.dto.resp.MissionGoodsResp;
import com.mall.common.model.dto.resp.MissionTaskResp;
import com.mall.common.model.dto.resp.MissionUserTaskResp;
import com.mall.mission.model.dto.MissionSubmitDTO;
import com.mall.mission.model.vo.MissionInvestProjectVO;
import com.mall.mission.model.vo.MissionTaskStatsVO;
import com.mall.mission.model.vo.MissionTaskVO;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

public interface MissionService {

    Page<MissionGoodsResp> goodsPage(MissionGoodsQueryReq req) throws BizException;

    MissionGoodsResp saveGoods(MissionGoodsReq req) throws BizException;

    void deleteGoods(Long id) throws BizException;

    Page<MissionTaskResp> taskPage(MissionTaskQueryReq req) throws BizException;

    MissionTaskResp saveTask(MissionTaskReq req) throws BizException;

    void deleteTask(Long id) throws BizException;

    Page<MissionUserTaskResp> userTaskPage(MissionUserTaskQueryReq req) throws BizException;

    List<LeaderboardItemResp> taskLeaderboard(Long startTime, Long endTime, Integer limit) throws BizException;

    MissionUserTaskResp approve(MissionTaskReviewReq req) throws BizException;

    MissionUserTaskResp reject(MissionTaskReviewReq req) throws BizException;

    MissionTaskStatsVO userStats(Long userId, String taskType) throws BizException;

    List<MissionTaskVO> userTasks(Long userId, String status, String taskType, Integer limit) throws BizException;

    MissionUserTaskResp claim(Long userId, Long taskId) throws BizException;

    MissionUserTaskResp submit(Long userId, MissionSubmitDTO dto) throws BizException;

    List<MissionUserTaskResp> records(Long userId, String status, String taskType, Integer limit) throws BizException;

    List<MissionInvestProjectVO> investProjects(Integer limit) throws BizException;
}