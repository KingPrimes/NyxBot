package com.nyx.bot.aop;

import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.utils.I18nUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;


@Component
public class NotEmptyValidator implements ConstraintValidator<NotEmpty, Object> {

    private String message;

    @Override
    public void initialize(NotEmpty constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value instanceof String) {
            if (((String) value).isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(I18nUtils.message(this.message))
                        .addConstraintViolation();
                return false;
            }
        }
        if (value == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(I18nUtils.message(this.message))
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
