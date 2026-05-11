package com.edelmetalle.notifier.pricemonitor.service;

import com.edelmetalle.notifier.pricemonitor.dto.MarketSignalDto;
import com.edelmetalle.notifier.pricemonitor.engine.RuleMatcher;
import com.edelmetalle.notifier.pricemonitor.enums.RuleOperator;
import com.edelmetalle.notifier.pricemonitor.model.NotificationTemplate;
import com.edelmetalle.notifier.pricemonitor.model.Recipient;
import com.edelmetalle.notifier.pricemonitor.model.Rule;
import com.edelmetalle.notifier.pricemonitor.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PriceMonitorServiceTest {

    @Mock
    private NotificationTemplateRepository repository;

    @Mock
    private RuleMatcher ruleMatcher;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PriceMonitorService priceMonitorService;

    private MarketSignalDto signal;
    private NotificationTemplate template;

    @BeforeEach
    void setUp() {
        signal = new MarketSignalDto("Gold", new BigDecimal("2500"));

        template = NotificationTemplate.builder()
                .title("Gold Alert")
                .content("Price is high!")
                .build();
        template.addRecipient(Recipient.builder().email("user@example.com").build());
    }

    @Test
    void shouldSendNotificationWhenAllRulesMatch() {
        // Given
        Rule rule = Rule.builder()
                .operator(RuleOperator.PRICE_GREATER)
                .operand("2000")
                .build();
        template.addRule(rule);

        when(repository.findAllWithRulesAndRecipients()).thenReturn(List.of(template));
        when(ruleMatcher.matches(any(), any())).thenReturn(true);

        // When
        priceMonitorService.processSignal(signal);

        // Then
        verify(notificationService, times(1)).sendNotification(
                eq("user@example.com"),
                eq("Gold Alert"),
                eq("Price is high!")
        );
    }

    @Test
    void shouldNOTSendNotificationWhenRulesDoNotMatch() {
        // Given
        Rule rule = Rule.builder()
                .operator(RuleOperator.PRICE_GREATER)
                .operand("3000") // Signal price 2500, so rule NOT met
                .build();
        template.addRule(rule);

        when(repository.findAllWithRulesAndRecipients()).thenReturn(List.of(template));
        when(ruleMatcher.matches(any(), any())).thenReturn(false);

        // When
        priceMonitorService.processSignal(signal);

        // Then
        verify(notificationService, never()).sendNotification(any(), any(), any());
    }

    @Test
    void shouldNotSendNotificationWhenTemplateHasNoRules() {
        // Given
        when(repository.findAllWithRulesAndRecipients()).thenReturn(List.of(template));

        // When
        priceMonitorService.processSignal(signal);

        // Then
        verify(notificationService, never()).sendNotification(any(), any(), any());
        verify(ruleMatcher, never()).matches(any(), any());
    }

    @Test
    void shouldSendToMultipleRecipients() {
        // Given
        Rule rule = Rule.builder()
                .operator(RuleOperator.PRICE_GREATER)
                .operand("2000")
                .build();
        template.addRule(rule);
        template.addRecipient(Recipient.builder().email("user2@example.com").build());
        template.addRecipient(Recipient.builder().email("user3@example.com").build());

        when(repository.findAllWithRulesAndRecipients()).thenReturn(List.of(template));
        when(ruleMatcher.matches(any(), any())).thenReturn(true);

        // When
        priceMonitorService.processSignal(signal);

        // Then
        verify(notificationService, times(3)).sendNotification(
                anyString(),
                eq("Gold Alert"),
                eq("Price is high!")
        );
    }

    @Test
    void shouldProcessMultipleTemplates() {
        // Given
        Rule rule = Rule.builder()
                .operator(RuleOperator.PRICE_GREATER)
                .operand("2000")
                .build();
        template.addRule(rule);

        NotificationTemplate template2 = NotificationTemplate.builder()
                .title("Silver Alert")
                .content("Silver is high!")
                .build();
        template2.addRule(Rule.builder()
                .operator(RuleOperator.PRICE_GREATER)
                .operand("1500")
                .build());
        template2.addRecipient(Recipient.builder().email("silver@example.com").build());

        when(repository.findAllWithRulesAndRecipients()).thenReturn(List.of(template, template2));
        when(ruleMatcher.matches(any(), any())).thenReturn(true);

        // When
        priceMonitorService.processSignal(signal);

        // Then
        verify(notificationService, times(1)).sendNotification(
                eq("user@example.com"),
                eq("Gold Alert"),
                eq("Price is high!")
        );
        verify(notificationService, times(1)).sendNotification(
                eq("silver@example.com"),
                eq("Silver Alert"),
                eq("Silver is high!")
        );
    }

    @Test
    void shouldNotSendNotificationWhenTemplateHasNoRecipients() {
        // Given
        Rule rule = Rule.builder()
                .operator(RuleOperator.PRICE_GREATER)
                .operand("2000")
                .build();
        template.addRule(rule);

        template.getRecipients().clear();

        when(repository.findAllWithRulesAndRecipients()).thenReturn(List.of(template));
        when(ruleMatcher.matches(any(), any())).thenReturn(true);

        // When
        priceMonitorService.processSignal(signal);

        // Then
        verify(notificationService, never()).sendNotification(any(), any(), any());
    }
}