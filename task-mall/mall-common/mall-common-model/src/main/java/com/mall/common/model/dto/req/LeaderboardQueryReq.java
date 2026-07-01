package com.mall.common.model.dto.req;

import lombok.Data;

/** 排行榜查询：type 指定榜单，limit 限制返回条数。 */
@Data
public class LeaderboardQueryReq {

    /** EARNING / RECHARGE / TASK。 */
    private String type;

    /** 起始时间（UTC毫秒），为空则不过滤。 */
    private Long startTime;

    /** 结束时间（UTC毫秒），为空则不过滤。 */
    private Long endTime;

    /** 返回条数，服务端会限制上限。 */
    private Integer limit;
}