package com.mall.common.core.valid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnumValidator implements ConstraintValidator<EnumValid, Object> {

    private Set<String> allowedValues;

    @Override
    public void initialize(EnumValid annotation) {
        allowedValues = Stream.of(annotation.value()).collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return allowedValues.contains(value.toString());
    }
}
