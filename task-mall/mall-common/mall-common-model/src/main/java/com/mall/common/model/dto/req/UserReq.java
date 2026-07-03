package com.mall.common.model.dto.req;

import com.mall.common.model.dto.BasePageDTO;
import lombok.Data;

@Data
public class UserReq extends BasePageDTO {

    private Long userId;

    private String userName;

    private String inviteCode;

    private Integer vipLevel;

    private Integer status;

    private Long groupId;

    private Long registerStartTime;

    private Long registerEndTime;
}
