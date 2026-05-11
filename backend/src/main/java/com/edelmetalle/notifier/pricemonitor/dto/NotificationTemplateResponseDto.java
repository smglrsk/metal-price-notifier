package com.edelmetalle.notifier.pricemonitor.dto;

import java.util.List;

public record NotificationTemplateResponseDto(
        Long id,
        String title,
        String content,
        List<RuleDto> rules,
        List<RecipientDto> recipients
) {
    public record RuleDto(
            Long id,
            String operator,
            String operand
    ) {}

    public record RecipientDto(
            Long id,
            String email
    ) {}
}