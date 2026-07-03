package com.mall.common.model.dto.resp;

import lombok.Data;

@Data
public class UserResp {

    private Long userId;

    private String userName;

    private String nickname;

    private String email;

    private Integer vipLevel;

    private String inviteCode;

    private Integer status;

    private String parentUserName;

    private Long parentUserId;

    private Long groupId;

    private String groupName;

    private Long registerTime;
}
