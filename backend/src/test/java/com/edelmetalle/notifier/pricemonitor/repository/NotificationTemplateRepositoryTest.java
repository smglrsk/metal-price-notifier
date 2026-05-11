package com.edelmetalle.notifier.pricemonitor.repository;

import com.edelmetalle.notifier.pricemonitor.enums.RuleOperator;
import com.edelmetalle.notifier.pricemonitor.model.NotificationTemplate;
import com.edelmetalle.notifier.pricemonitor.model.Recipient;
import com.edelmetalle.notifier.pricemonitor.model.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class NotificationTemplateRepositoryTest {

    @Autowired
    private NotificationTemplateRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldFindAllTemplatesWithRulesAndRecipients() {
        // Given
        NotificationTemplate template1 = NotificationTemplate.builder()
                .title("Gold Alert")
                .content("Gold content")
                .build();
        template1.addRule(Rule.builder()
                .operator(RuleOperator.ITEM_IS)
                .operand("gold")
                .build());
        template1.addRecipient(Recipient.builder()
                .email("gold@test.com")
                .build());

        NotificationTemplate template2 = NotificationTemplate.builder()
                .title("Silver Alert")
                .content("Silver content")
                .build();
        template2.addRule(Rule.builder()
                .operator(RuleOperator.PRICE_GREATER)
                .operand("1000")
                .build());
        template2.addRecipient(Recipient.builder()
                .email("silver@test.com")
                .build());

        repository.save(template1);
        repository.save(template2);

        // When
        List<NotificationTemplate> results = repository.findAllWithRulesAndRecipients();

        // Then
        assertEquals(2, results.size());

        NotificationTemplate found1 = results.stream()
                .filter(t -> "Gold Alert".equals(t.getTitle()))
                .findFirst()
                .orElseThrow();

        assertFalse(found1.getRules().isEmpty());
        assertFalse(found1.getRecipients().isEmpty());
        assertEquals(1, found1.getRules().size());
        assertEquals(1, found1.getRecipients().size());
    }

    @Test
    void shouldHandleEmptyResultWhenNoTemplates() {
        // When
        List<NotificationTemplate> results = repository.findAllWithRulesAndRecipients();

        // Then
        assertTrue(results.isEmpty());
    }

    @Test
    void shouldFindTemplateWithMultipleRules() {
        // Given
        NotificationTemplate template = NotificationTemplate.builder()
                .title("Complex Alert")
                .content("Complex content")
                .build();
        template.addRule(Rule.builder()
                .operator(RuleOperator.ITEM_IS)
                .operand("platinum")
                .build());
        template.addRule(Rule.builder()
                .operator(RuleOperator.PRICE_GREATER)
                .operand("5000")
                .build());
        template.addRecipient(Recipient.builder()
                .email("complex@test.com")
                .build());

        repository.save(template);

        // When
        List<NotificationTemplate> results = repository.findAllWithRulesAndRecipients();

        // Then
        assertEquals(1, results.size());
        NotificationTemplate found = results.get(0);
        assertEquals(2, found.getRules().size());
        assertTrue(found.getRules().stream()
                .anyMatch(r -> r.getOperator() == RuleOperator.ITEM_IS));
        assertTrue(found.getRules().stream()
                .anyMatch(r -> r.getOperator() == RuleOperator.PRICE_GREATER));
    }
}