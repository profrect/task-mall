package com.mall.admin.model.dto;

import com.mall.common.core.valid.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class BotConfigDTO implements Serializable {
    private static final long serialVersionUID = -569081893470663651L;

    @NotNull(message = "【id】不能为空", groups = {ValidGroups.Update.class})
    private Long id;

    @NotBlank(message = "参数【机器人名称】不能为空", groups = {ValidGroups.Insert.class, ValidGroups.Update.class})
    @Size(max = 64, message = "参数【机器人名称】超过长度限制")
    private String botName;

    @NotBlank(message = "参数【Token】不能为空", groups = {ValidGroups.Insert.class})
    @Size(max = 255, message = "参数【Token】超过长度限制")
    private String botToken;

    @Size(max = 255, message = "参数【Webhook】超过长度限制")
    private String webhookUrl;

    @Size(max = 255, message = "参数【说明】超过长度限制")
    private String description;

    private Integer sortOrder;

    private Integer status;
}