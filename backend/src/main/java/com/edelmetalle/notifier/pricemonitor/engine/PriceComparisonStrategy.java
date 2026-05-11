package com.edelmetalle.notifier.pricemonitor.engine;

import com.edelmetalle.notifier.pricemonitor.dto.MarketSignalDto;
import com.edelmetalle.notifier.pricemonitor.model.Rule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
public class PriceComparisonStrategy implements RuleStrategy {

    private static final Set<String> SUPPORTED_OPERATORS = Set.of(
            "PRICE_EQUAL",
            "PRICE_GREATER_OR_EQUAL",
            "PRICE_GREATER",
            "PRICE_LESS",
            "PRICE_LESS_OR_EQUAL"
    );

    @Override
    public boolean supports(String operator) {
        return SUPPORTED_OPERATORS.contains(operator);
    }

    @Override
    public boolean evaluate(Rule rule, MarketSignalDto signal) {
        BigDecimal rulePrice = new BigDecimal(rule.getOperand());
        int comparison = signal.price().compareTo(rulePrice);

        return switch (rule.getOperator()) {
            case PRICE_EQUAL -> comparison == 0;
            case PRICE_GREATER -> comparison > 0;
            case PRICE_GREATER_OR_EQUAL -> comparison >= 0;
            case PRICE_LESS -> comparison < 0;
            case PRICE_LESS_OR_EQUAL -> comparison <= 0;
            default -> throw new IllegalArgumentException("Unsupported price operator: " + rule.getOperator());
        };
    }
}
