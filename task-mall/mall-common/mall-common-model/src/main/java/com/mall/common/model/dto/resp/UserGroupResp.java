package com.mall.common.model.dto.resp;

import lombok.Data;

@Data
public class UserGroupResp {

    private Long id;

    private String groupName;

    private String remark;

    private Integer status;

    private Integer sortOrder;

    private Long memberCount;

    private Long createTime;

    private Long updateTime;
}