package com.mall.common.model.dto.resp;

import lombok.Data;

/** 会员统计快照（跨服务契约：mall-user provider → mall-admin 报表）。 */
@Data
public class UserStatsResp {

    /** 会员总数。 */
    private Long totalUsers;

    /** 今日新增会员数（按 user_info.create_time，本地自然日）。 */
    private Long todayNewUsers;
}