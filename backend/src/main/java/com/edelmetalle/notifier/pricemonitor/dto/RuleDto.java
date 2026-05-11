package com.edelmetalle.notifier.pricemonitor.dto;

import com.edelmetalle.notifier.pricemonitor.validator.ValidOperand;
import jakarta.validation.constraints.NotBlank;

public record RuleDto(
        @NotBlank(message = "Operator nie może być pusty")
        String operator,

        @NotBlank(message = "Operand nie może być pusty")
        @ValidOperand
        String operand
) {}
