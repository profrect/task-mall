package com.mall.admin.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminLoginVO {

    /**
     * 用户名
     */
    private String username;
    /**
     * 访问token
     */
    private String accessToken;
    /**
     * 过期时间（毫秒）
     */
    private Long expires;
    private String realName;
    private String email;
    private String roles;
    private Integer level;
}
