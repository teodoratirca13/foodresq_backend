package com.foodresq.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

public class RequiredWhenValidator implements ConstraintValidator<RequiredWhen, Object> {

    private String field;
    private String whenField;
    private String whenValue;
    private String message;

    @Override
    public void initialize(RequiredWhen annotation) {
        this.field = annotation.field();
        this.whenField = annotation.whenField();
        this.whenValue = annotation.whenValue();
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        try {
            Field whenFieldObj = value.getClass().getDeclaredField(whenField);
            whenFieldObj.setAccessible(true);
            Object whenValueActual = whenFieldObj.get(value);

            boolean conditionMatches = whenValueActual != null && whenValueActual.toString().equals(whenValue);

            if (!conditionMatches) {
                return true;
            }

            Field fieldObj = value.getClass().getDeclaredField(field);
            fieldObj.setAccessible(true);
            Object fieldVal = fieldObj.get(value);

            if (fieldVal == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
                return false;
            }

            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
