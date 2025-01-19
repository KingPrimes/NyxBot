package com.nyx.bot.annotation;


import com.nyx.bot.aop.NotEmptyValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotEmptyValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmpty {
    @jakarta.validation.constraints.NotEmpty String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
