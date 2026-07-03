package com.mall.admin.model.dto;

import com.mall.common.core.valid.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class BotAutoReplyDTO implements Serializable {
    private static final long serialVersionUID = 4133268686787947938L;

    @NotNull(message = "【id】不能为空", groups = {ValidGroups.Update.class})
    private Long id;

    @NotNull(message = "参数【机器人ID】不能为空", groups = {ValidGroups.Insert.class, ValidGroups.Update.class})
    private Long botId;

    @NotBlank(message = "参数【关键词】不能为空", groups = {ValidGroups.Insert.class, ValidGroups.Update.class})
    @Size(max = 128, message = "参数【关键词】超过长度限制")
    private String keyword;

    @NotBlank(message = "参数【回复内容】不能为空", groups = {ValidGroups.Insert.class, ValidGroups.Update.class})
    @Size(max = 2000, message = "参数【回复内容】超过长度限制")
    private String replyContent;

    private Integer matchType;

    private Integer sortOrder;

    private Integer status;
}