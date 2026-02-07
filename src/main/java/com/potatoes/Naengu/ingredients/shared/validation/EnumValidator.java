package com.potatoes.Naengu.ingredients.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<EnumValue,String> {

    private EnumValue annotation;

    @Override
    public void initialize(EnumValue constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        Object[] enumValues = annotation.enumClass().getEnumConstants();
        if (enumValues == null) return false;

        for (Object enumValue : enumValues) {
            String enumName = enumValue.toString();
            if (annotation.ignoreCase()) {
                if (value.equalsIgnoreCase(enumName)) return true;
                continue;
            }
            if (value.equals(enumName)) return true;
        }
        return false;
    }
}
