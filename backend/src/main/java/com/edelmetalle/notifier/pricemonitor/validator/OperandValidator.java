package com.edelmetalle.notifier.pricemonitor.validator;

import com.edelmetalle.notifier.pricemonitor.config.PriceConfig;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class OperandValidator implements ConstraintValidator<ValidOperand, String> {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");
    private final PriceConfig priceConfig;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Operand nie może być pusty")
                    .addConstraintViolation();
            return false;
        }

        String normalizedValue = value.trim().toLowerCase();

        boolean isSupportedItem = priceConfig.getSupportedItems().contains(normalizedValue);

        boolean isValidNumber = NUMERIC_PATTERN.matcher(value).matches();

        if (!isSupportedItem && !isValidNumber) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Operand musi być wspieranym metalem (gold/silver/platinum) lub liczbą z maksymalnie 2 miejscami po przecinku"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}