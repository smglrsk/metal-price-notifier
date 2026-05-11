package com.edelmetalle.notifier.pricemonitor.dto;


import com.edelmetalle.notifier.pricemonitor.validator.ValidOperand;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record MarketSignalDto(
        @NotBlank
        @ValidOperand
        String itemType,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal price
) {}