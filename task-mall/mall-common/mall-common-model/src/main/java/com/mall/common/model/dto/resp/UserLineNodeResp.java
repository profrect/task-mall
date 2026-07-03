package com.mall.common.model.dto.resp;

import lombok.Data;

@Data
public class UserLineNodeResp {

    private Long userId;

    private String userName;

    private String nickname;

    private Integer vipLevel;

    private Integer status;

    private Long parentUserId;

    private String parentUserName;

    /** `SELF`、`PARENT`、`CHILD`。 */
    private String relation;

    /** 距离当前会员的层级，当前会员为 0。 */
    private Integer depth;
}