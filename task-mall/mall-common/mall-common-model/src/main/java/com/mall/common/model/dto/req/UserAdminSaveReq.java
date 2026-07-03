package com.mall.common.model.dto.req;

import lombok.Data;

@Data
public class UserAdminSaveReq {

    /** 为空表示新增；非空表示编辑指定会员。 */
    private Long userId;

    /** 登录账号：新增必填；编辑时可改名。 */
    private String userName;

    /** 登录密码：新增必填；编辑为空表示不重置密码。 */
    private String password;

    private String nickname;

    private String email;

    private Integer vipLevel;

    private Integer status;

    /** 上级会员 userId；为空表示无上级。 */
    private Long parentUserId;

    /** 会员分组；为空表示不绑定分组。 */
    private Long groupId;
}