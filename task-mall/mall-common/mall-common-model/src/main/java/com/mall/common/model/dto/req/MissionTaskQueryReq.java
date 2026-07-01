package com.mall.common.model.dto.req;

import com.mall.common.model.dto.BasePageDTO;
import lombok.Data;

/** 后台/Provider 查询任务配置。 */
@Data
public class MissionTaskQueryReq extends BasePageDTO {

    private String keyword;

    private String taskType;

    private Integer status;
}