package com.mall.admin.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 后台内容配置类型。单页类只保留一条当前内容；NOTICE 允许多条公告。 */
public enum ContentItemType {
    COMPANY_PROFILE,
    PLATFORM_PROFILE,
    REGULATOR,
    NOTICE,
    USER_AGREEMENT,
    USER_PRIVACY;

    private static final Set<String> NAMES = Arrays.stream(values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    public static boolean valid(String value) {
        return value != null && NAMES.contains(value);
    }

    public static boolean singleton(String value) {
        return valid(value) && !NOTICE.name().equals(value);
    }
}