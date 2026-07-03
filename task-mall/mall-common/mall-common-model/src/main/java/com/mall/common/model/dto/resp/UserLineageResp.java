package com.mall.common.model.dto.resp;

import lombok.Data;

import java.util.List;

@Data
public class UserLineageResp {

    private UserLineNodeResp current;

    private List<UserLineNodeResp> ancestors;

    private List<UserLineNodeResp> children;
}