package com.edelmetalle.notifier.pricemonitor.enums;

import java.util.Arrays;

public enum RuleOperator {
    ITEM_IS("Item is"),
    ITEM_IS_NOT("Item is not"),
    PRICE_EQUAL("Price is equal to"),
    PRICE_GREATER_OR_EQUAL("Price is greater than or equal to"),
    PRICE_GREATER("Price is greater than"),
    PRICE_LESS("Price is less than"),
    PRICE_LESS_OR_EQUAL("Price is less than or equal to");

    private final String description;

    RuleOperator(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static RuleOperator fromDescription(String text) {
        return Arrays.stream(RuleOperator.values())
                .filter(op -> op.description.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown operator: " + text));
    }
}