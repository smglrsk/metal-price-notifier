package com.edelmetalle.notifier.pricemonitor.engine;

import com.edelmetalle.notifier.pricemonitor.dto.MarketSignalDto;
import com.edelmetalle.notifier.pricemonitor.model.Rule;
import org.springframework.stereotype.Component;

@Component
public class ItemComparisonStrategy implements RuleStrategy {

    @Override
    public boolean supports(String operator) {
        return "ITEM_IS".equals(operator) || "ITEM_IS_NOT".equals(operator);
    }

    @Override
    public boolean evaluate(Rule rule, MarketSignalDto signal) {
        boolean isSameItem = signal.itemType().equalsIgnoreCase(rule.getOperand());

        return switch (rule.getOperator()) {
            case ITEM_IS -> isSameItem;
            case ITEM_IS_NOT -> !isSameItem;
            default -> throw new IllegalArgumentException("Unsupported item operator: " + rule.getOperator());
        };
    }
}