package com.nyx.bot.aop;

import com.nyx.bot.annotation.InternationalizedNotEmpty;
import com.nyx.bot.utils.I18nUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InternationalizedNotEmptyValidator implements ConstraintValidator<InternationalizedNotEmpty, String> {

    private String message;

    @Override
    public void initialize(InternationalizedNotEmpty constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(I18nUtils.message(this.message))
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
