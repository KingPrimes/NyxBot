package com.nyx.bot.annotation;


import com.nyx.bot.aop.InternationalizedNotEmptyValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotEmpty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = InternationalizedNotEmptyValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface InternationalizedNotEmpty {
    @NotEmpty String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
