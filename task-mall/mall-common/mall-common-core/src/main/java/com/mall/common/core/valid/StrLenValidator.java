package com.mall.common.core.valid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StrLenValidator implements ConstraintValidator<StrLen, Object> {

    private int maxLen;

    private int minLen;

    @Override
    public void initialize(StrLen annotation) {
        maxLen = annotation.maxLen();
        minLen = annotation.minLen();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null || !StringUtils.hasText(value.toString())) {
            return true;
        }
        int length = value.toString().length();
        return length >= minLen && length <= maxLen;
    }
}
