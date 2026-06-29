package com.mall.admin.model.dto;


import com.mall.common.core.valid.ValidGroups;
import com.mall.common.model.dto.BasePageDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ManageApiInfoDTO extends BasePageDTO {

    @NotNull(message = "【id】不能为空", groups = {ValidGroups.Update.class})
    private Long id;

    @NotBlank(message = "参数【接口名称】不能为空", groups = {ValidGroups.Insert.class})
    private String apiName;

    @NotBlank(message = "参数【接口地址】不能为空", groups = {ValidGroups.Insert.class})
    private String apiUrl;

    @NotBlank(message = "参数【HTTP方法】不能为空", groups = {ValidGroups.Insert.class})
    @Pattern(
            regexp = "^(?i)(get|post|delete|put)$",
            message = "参数【HTTP方法】只能为 get、post、delete、put"
    )
    private String method;

    @NotNull(message = "参数【接口状态】不能为空", groups = {ValidGroups.Insert.class})
    private Integer status;

    @NotNull(message = "参数【接口类型】不能为空", groups = {ValidGroups.Insert.class})
    private Integer type;

    private String remark;
}

