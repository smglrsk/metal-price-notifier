package com.edelmetalle.notifier.pricemonitor;

import com.edelmetalle.notifier.pricemonitor.dto.MarketSignalDto;
import com.edelmetalle.notifier.pricemonitor.enums.RuleOperator;
import com.edelmetalle.notifier.pricemonitor.model.NotificationTemplate;
import com.edelmetalle.notifier.pricemonitor.model.Recipient;
import com.edelmetalle.notifier.pricemonitor.model.Rule;
import com.edelmetalle.notifier.pricemonitor.repository.NotificationTemplateRepository;
import com.edelmetalle.notifier.pricemonitor.service.NotificationService;
import com.edelmetalle.notifier.pricemonitor.service.PriceMonitorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class PriceMonitorIntegrationTest {

    @Autowired
    private PriceMonitorService priceMonitorService;

    @Autowired
    private NotificationTemplateRepository repository;

    @MockitoSpyBean
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {

        repository.deleteAll();
    }

    @Test
    @DisplayName("Should NOT send notification when price rule not met")
    void shouldNotSendWhenPriceTooLow() {
        // Given
        NotificationTemplate template = NotificationTemplate.builder()
                .title("Test Gold Alert")
                .content("Test gold price alert!")
                .build();

        Rule rule1 = Rule.builder()
                .operator(RuleOperator.ITEM_IS)
                .operand("gold")
                .build();

        Rule rule2 = Rule.builder()
                .operator(RuleOperator.PRICE_GREATER)
                .operand("2000")
                .build();

        template.addRule(rule1);
        template.addRule(rule2);
        template.addRecipient(Recipient.builder().email("test@example.com").build());

        repository.save(template);

        // When
        MarketSignalDto lowPriceSignal = new MarketSignalDto("gold", new BigDecimal("1500"));
        priceMonitorService.processSignal(lowPriceSignal);

        // Then
        verify(notificationService, never()).sendNotification(any(), any(), any());
    }

    @Test
    @DisplayName("Should send notification when all conditions are met")
    void shouldSendWhenConditionsMet() throws Exception {
        // Given
        NotificationTemplate template = NotificationTemplate.builder()
                .title("Test Gold Alert")
                .content("Gold price is high!")
                .build();

        template.addRule(Rule.builder()
                .operator(RuleOperator.ITEM_IS)
                .operand("gold")
                .build());
        template.addRule(Rule.builder()
                .operator(RuleOperator.PRICE_GREATER)
                .operand("2000")
                .build());
        template.addRecipient(Recipient.builder().email("test@example.com").build());

        repository.save(template);

        // When
        MarketSignalDto signal = new MarketSignalDto("gold", new BigDecimal("2500"));
        priceMonitorService.processSignal(signal);

        // Then
        verify(notificationService, times(1)).sendNotification(
                eq("test@example.com"),
                eq("Test Gold Alert"),
                eq("Gold price is high!")
        );
    }
}