package com.mall.user.service;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.BasePageDTO;
import com.mall.common.model.dto.req.UserReq;
import com.mall.common.model.dto.resp.UserExistResp;
import com.mall.common.model.dto.resp.UserProfileSummaryResp;
import com.mall.common.model.dto.resp.UserResp;
import com.mall.common.model.dto.resp.UserStatsResp;
import com.mall.user.model.dto.UserInfoDTO;
import com.mall.user.model.entity.UserInfo;
import com.mall.user.model.vo.TeamMembersVo;
import com.mall.user.model.vo.UserDetailVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 用户资料域：本人详情 / 资料维护 / 直属团队，以及供管理端的会员分页。
 * 所有"本人"操作的 userId 均由调用方从会话解析后传入，service 不信任任何前端传入的身份。
 */
public interface UserInfoService extends IService<UserInfo> {

    /** 管理端会员分页（provider）。 */
    Page<UserResp> pageList(UserReq req);

    /** 当前登录用户的资料详情（聚合登录账号、邀请人、直属团队人数）。 */
    UserDetailVO currentUserDetail(long userId) throws BizException;

    /** 会员统计快照：总会员数 + 今日新增。 */
    UserStatsResp stats(long todayStartAt);

    /** 会员存在性快照：供下游服务校验收款人/业务归属，不暴露敏感资料。 */
    UserExistResp exists(Long userId);

    /** 批量公开展示资料：供榜单等只读聚合脱敏展示。 */
    List<UserProfileSummaryResp> profileSummaries(List<Long> userIds);

    /** 管理端变更会员状态：1=正常，2=冻结。冻结会失效该用户现有登录态。 */
    void updateStatus(Long userId, Integer status) throws BizException;

    /** 更新当前用户资料：仅允许昵称 / 邮箱，userId 取自会话。 */
    void updateUserInfo(long userId, UserInfoDTO dto) throws BizException;

    /** 当前用户的直属团队成员分页（inviter = 当前用户，按加入时间倒序）。 */
    Page<TeamMembersVo> teamMembers(long userId, BasePageDTO page);
}