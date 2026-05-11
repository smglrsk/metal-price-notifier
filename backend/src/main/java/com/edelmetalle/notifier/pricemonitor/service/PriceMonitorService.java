package com.edelmetalle.notifier.pricemonitor.service;

import com.edelmetalle.notifier.pricemonitor.dto.MarketSignalDto;
import com.edelmetalle.notifier.pricemonitor.engine.RuleMatcher;
import com.edelmetalle.notifier.pricemonitor.model.NotificationTemplate;
import com.edelmetalle.notifier.pricemonitor.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceMonitorService {

    private final NotificationTemplateRepository repository;
    private final RuleMatcher ruleMatcher;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public void processSignal(MarketSignalDto signal) {
        log.info("Przetwarzanie sygnału: {} po cenie {}", signal.itemType(), signal.price());

        List<NotificationTemplate> allTemplates = repository.findAllWithRulesAndRecipients();

        log.debug("Znaleziono {} szablonów do sprawdzenia", allTemplates.size());

        long matchingCount = allTemplates.stream()
                .filter(template -> matchesAllRules(template, signal))
                .peek(this::sendNotifications)
                .count();

        log.info("Znaleziono {} pasujących szablonów dla sygnału", matchingCount);
    }

    private boolean matchesAllRules(NotificationTemplate template, MarketSignalDto signal) {
        if (template.getRules().isEmpty()) {
            log.warn("Szablon '{}' nie ma żadnych reguł - nigdy nie zostanie wyzwolony", template.getTitle());
            return false;
        }

        boolean allMatch = template.getRules().stream()
                .allMatch(rule -> ruleMatcher.matches(rule, signal));

        if (allMatch) {
            log.info("Szablon '{}' spełnia wszystkie reguły", template.getTitle());
        }

        return allMatch;
    }

    private void sendNotifications(NotificationTemplate template) {
        log.info("Wysyłanie powiadomień dla szablonu: {}", template.getTitle());

        if (template.getRecipients().isEmpty()) {
            log.warn("Szablon '{}' nie ma zdefiniowanych odbiorców!", template.getTitle());
            return;
        }

        template.getRecipients().forEach(recipient -> {
            notificationService.sendNotification(
                    recipient.getEmail(),
                    template.getTitle(),
                    template.getContent()
            );
        });
    }
}