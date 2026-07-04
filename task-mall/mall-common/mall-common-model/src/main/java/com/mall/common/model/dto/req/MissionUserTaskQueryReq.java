package com.mall.common.model.dto.req;

import com.mall.common.model.dto.BasePageDTO;
import lombok.Data;

/** 后台/Provider 查询用户任务记录。 */
@Data
public class MissionUserTaskQueryReq extends BasePageDTO {

    private Long userId;

    private Long taskId;

    private String taskType;

    private String status;

    private String keyword;
}