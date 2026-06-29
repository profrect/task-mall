package com.mall.admin.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminLoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 32, message = "用户名输入错误")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String userPswd;
}
