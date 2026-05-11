package com.edelmetalle.notifier.pricemonitor.dto;

import com.edelmetalle.notifier.pricemonitor.validator.ValidOperand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record NotificationTemplateDto(
        Long id,
        @NotBlank String title,
        @NotBlank String content,
        @NotEmpty @Valid List<RuleDto> rules,
        @NotEmpty @Valid List<RecipientDto> recipients
) {
    public record RuleDto(
            Long id,
            @NotBlank String operator,
            @NotBlank @ValidOperand String operand
    ) {}

    public record RecipientDto(
            Long id,
            @NotBlank @Email String email
    ) {}
}