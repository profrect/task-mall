package com.mall.admin.model.dto;


import com.mall.common.core.valid.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ManageUserInfoDTO implements Serializable {
    private static final long serialVersionUID = -17574651325389653L;

    @NotNull(message = "参数【ID】不能为空", groups = {ValidGroups.Update.class})
    private Long id;

    @NotBlank(message = "参数【用户名】不能为空", groups = {ValidGroups.Insert.class})
    private String username;

    private String realName;
    private String password;
    private String email;
    private Integer level;

    private List<String> roleCodeList;
}

