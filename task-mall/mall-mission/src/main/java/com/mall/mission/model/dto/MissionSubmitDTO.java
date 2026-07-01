package com.mall.mission.model.dto;

import lombok.Data;

/** 用户提交任务证明。 */
@Data
public class MissionSubmitDTO {

    private Long recordId;

    private String submitContent;
}