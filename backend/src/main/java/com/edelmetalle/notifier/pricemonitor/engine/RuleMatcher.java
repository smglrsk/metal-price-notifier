package com.edelmetalle.notifier.pricemonitor.engine;

import com.edelmetalle.notifier.pricemonitor.dto.MarketSignalDto;
import com.edelmetalle.notifier.pricemonitor.model.Rule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleMatcher {
    private final List<RuleStrategy> strategies;

    public boolean matches(Rule rule, MarketSignalDto signal) {
        String operatorName = rule.getOperator().name();

        return strategies.stream()
                .filter(s -> s.supports(operatorName))
                .findFirst()
                .map(s -> {
                    try {
                        return s.evaluate(rule, signal);
                    } catch (Exception e) {
                        log.error("Błąd podczas ewaluacji reguły {} z operandem {}: {}",
                                operatorName, rule.getOperand(), e.getMessage());
                        return false;
                    }
                })
                .orElseGet(() -> {
                    // Zamiast rzucać wyjątkiem - logujemy i zwracamy false
                    log.warn("Nie znaleziono strategii dla operatora: {}. Reguła zostanie zignorowana.", operatorName);
                    return false;
                });
    }
}