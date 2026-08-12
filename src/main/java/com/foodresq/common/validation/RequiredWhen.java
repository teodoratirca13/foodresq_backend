package com.foodresq.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = RequiredWhenValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredWhen {

    String message() default "This field is required";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String field();

    String whenField();

    String whenValue();
}
