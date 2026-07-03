package com.mall.common.model.dto.req;

import lombok.Data;

import java.util.List;

@Data
public class UserGroupAssignReq {

    private List<Long> userIds;

    /** 为空表示移出当前分组。 */
    private Long groupId;
}