package com.mall.common.model.dto.resp;

import lombok.Data;

@Data
public class UserResp {

    private Long userId;

    private String userName;

    private Integer vipLevel;

    private String inviteCode;

    private Integer status;

    private String parentUserName;

    private Long registerTime;
}
