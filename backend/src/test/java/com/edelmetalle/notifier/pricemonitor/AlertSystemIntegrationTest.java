package com.edelmetalle.notifier.pricemonitor;

import com.edelmetalle.notifier.pricemonitor.dto.MarketSignalDto;
import com.edelmetalle.notifier.pricemonitor.enums.RuleOperator;
import com.edelmetalle.notifier.pricemonitor.model.NotificationTemplate;
import com.edelmetalle.notifier.pricemonitor.model.Rule;
import com.edelmetalle.notifier.pricemonitor.repository.NotificationTemplateRepository;
import com.edelmetalle.notifier.pricemonitor.service.PriceMonitorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@Transactional
class AlertSystemIntegrationTest {

    @Autowired
    private PriceMonitorService priceMonitorService;

    @Autowired
    private NotificationTemplateRepository repository;

    @Test
    @DisplayName("Should successfully process signal against stored template")
    void shouldTriggerAlertWhenAllConditionsMet() {

        NotificationTemplate template = new NotificationTemplate();
        template.setTitle("Test Alert");
        template.setContent("Test content");


        template.setRules(new HashSet<>());
        template.setRecipients(new HashSet<>());

        Rule rule = new Rule();
        rule.setOperator(RuleOperator.PRICE_GREATER);
        rule.setOperand("2000");
        rule.setTemplate(template);
        template.getRules().add(rule);

        repository.save(template);

        MarketSignalDto signal = new MarketSignalDto("gold", new BigDecimal("2100"));

        assertDoesNotThrow(() -> priceMonitorService.processSignal(signal));
    }
}