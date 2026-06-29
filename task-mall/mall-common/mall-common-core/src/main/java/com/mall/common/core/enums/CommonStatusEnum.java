package com.mall.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonStatusEnum {

    STATUS_YES(1),
    STATUS_NO(0),

    ;

    private final int code;
}
