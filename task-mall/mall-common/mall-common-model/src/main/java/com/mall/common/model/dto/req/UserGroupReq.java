package com.mall.common.model.dto.req;

import lombok.Data;

@Data
public class UserGroupReq {

    private Long id;

    private String groupName;

    private String remark;

    private Integer status;

    private Integer sortOrder;
}