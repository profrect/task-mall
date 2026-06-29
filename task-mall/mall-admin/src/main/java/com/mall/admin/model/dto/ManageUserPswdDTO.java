package com.mall.admin.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ManageUserPswdDTO {
    @NotNull(message = "【用户名】不能为空")
    private String username;

    @NotNull(message = "【新密码】不能为空")
    private String newPswd;

    @NotNull(message = "【旧密码】不能为空")
    private String oldPswd;
}
