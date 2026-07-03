package com.mall.user.service;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.BasePageDTO;
import com.mall.common.model.dto.req.UserAdminSaveReq;
import com.mall.common.model.dto.req.UserLineChangeReq;
import com.mall.common.model.dto.req.UserReq;
import com.mall.common.model.dto.resp.UserExistResp;
import com.mall.common.model.dto.resp.UserLineageResp;
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

    /** 管理端会员详情（provider）。 */
    UserResp detail(Long userId) throws BizException;

    /** 管理端新增/编辑会员：账号、密码、状态、VIP、上级、分组统一在用户域内落库。 */
    UserResp adminSave(UserAdminSaveReq req) throws BizException;

    /** 管理端查看会员上下级链路。 */
    UserLineageResp lineage(Long userId) throws BizException;

    /** 管理端调整会员上级，禁止形成环。 */
    void changeParent(UserLineChangeReq req) throws BizException;

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

    /** 管理端批量变更会员状态：1=正常，2=冻结。冻结会失效会员现有登录态。 */
    void updateStatusBatch(List<Long> userIds, Integer status) throws BizException;

    /** 管理端强制会员当前登录态失效。 */
    void logout(Long userId) throws BizException;

    /** 更新当前用户资料：仅允许昵称 / 邮箱，userId 取自会话。 */
    void updateUserInfo(long userId, UserInfoDTO dto) throws BizException;

    /** 当前用户的直属团队成员分页（inviter = 当前用户，按加入时间倒序）。 */
    Page<TeamMembersVo> teamMembers(long userId, BasePageDTO page);
}