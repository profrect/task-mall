package com.mall.admin.model.dto;

import com.mall.common.core.valid.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class SystemParamDTO implements Serializable {
    private static final long serialVersionUID = -6759477991732201850L;

    @NotNull(message = "【id】不能为空", groups = {ValidGroups.Update.class})
    private Long id;

    @NotBlank(message = "参数【参数键】不能为空", groups = {ValidGroups.Insert.class, ValidGroups.Update.class})
    @Size(max = 96, message = "参数【参数键】超过长度限制")
    @Pattern(regexp = "^[a-z][a-z0-9_\\-.]*$", message = "参数【参数键】格式无效")
    private String paramKey;

    @Size(max = 2000, message = "参数【参数值】超过长度限制")
    private String paramValue;

    @Size(max = 255, message = "参数【说明】超过长度限制")
    private String description;

    private Integer sortOrder;

    private Integer status;
}