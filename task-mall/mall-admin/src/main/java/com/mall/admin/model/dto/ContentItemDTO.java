package com.mall.admin.model.dto;

import com.mall.common.core.valid.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ContentItemDTO implements Serializable {
    private static final long serialVersionUID = 3840468924499680033L;

    @NotNull(message = "【id】不能为空", groups = {ValidGroups.Update.class})
    private Long id;

    @NotBlank(message = "参数【内容类型】不能为空", groups = {ValidGroups.Insert.class, ValidGroups.Update.class})
    @Size(max = 32, message = "参数【内容类型】超过长度限制")
    private String type;

    @Size(max = 16, message = "参数【语言】超过长度限制")
    private String languageCode;

    @NotBlank(message = "参数【标题】不能为空", groups = {ValidGroups.Insert.class, ValidGroups.Update.class})
    @Size(max = 128, message = "参数【标题】超过长度限制")
    private String title;

    @Size(max = 255, message = "参数【摘要】超过长度限制")
    private String summary;

    @NotBlank(message = "参数【正文】不能为空", groups = {ValidGroups.Insert.class, ValidGroups.Update.class})
    private String content;

    private Integer sortOrder;

    private Integer status;
}