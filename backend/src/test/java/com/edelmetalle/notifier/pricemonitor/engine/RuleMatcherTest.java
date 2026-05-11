package com.edelmetalle.notifier.pricemonitor.engine;

import com.edelmetalle.notifier.pricemonitor.dto.MarketSignalDto;
import com.edelmetalle.notifier.pricemonitor.enums.RuleOperator;
import com.edelmetalle.notifier.pricemonitor.model.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuleMatcherTest {

    private RuleMatcher ruleMatcher;

    @BeforeEach
    void setUp() {

        ruleMatcher = new RuleMatcher(List.of(
                new PriceComparisonStrategy(),
                new ItemComparisonStrategy()
        ));
    }

    @ParameterizedTest(name = "Operator {0}: Signal {1} vs Operand {2} should be {3}")
    @CsvSource({

            "PRICE_GREATER,          2100, 2000, true",
            "PRICE_GREATER,          1900, 2000, false",
            "PRICE_LESS,             1900, 2000, true",
            "PRICE_LESS,             2100, 2000, false",
            "PRICE_EQUAL,            2000, 2000, true",
            "PRICE_EQUAL,            2001, 2000, false",
            "PRICE_GREATER_OR_EQUAL, 2000, 2000, true",
            "PRICE_GREATER_OR_EQUAL, 2100, 2000, true",
            "PRICE_GREATER_OR_EQUAL, 1900, 2000, false",
            "PRICE_LESS_OR_EQUAL,    2000, 2000, true",
            "PRICE_LESS_OR_EQUAL,    1900, 2000, true",
            "PRICE_LESS_OR_EQUAL,    2100, 2000, false"
    })
    void shouldMatchPriceOperators(RuleOperator operator, String signalPrice, String operand, boolean expectedResult) {
        // GIVEN
        MarketSignalDto signal = new MarketSignalDto("Gold", new BigDecimal(signalPrice));
        Rule rule = Rule.builder().operator(operator).operand(operand).build();

        // WHEN
        boolean result = ruleMatcher.matches(rule, signal);

        // THEN
        assertEquals(expectedResult, result);
    }

    @ParameterizedTest(name = "Operator {0}: Signal {1} vs Operand {2} should be {3}")
    @CsvSource({

            "ITEM_IS,     Gold, Gold, true",
            "ITEM_IS,     Gold, Silver, false",
            "ITEM_IS,     GOLD, gold, true",
            "ITEM_IS_NOT, Gold, Silver, true",
            "ITEM_IS_NOT, Gold, Gold, false"
    })
    void shouldMatchItemOperators(RuleOperator operator, String signalItem, String operand, boolean expectedResult) {
        // GIVEN
        MarketSignalDto signal = new MarketSignalDto(signalItem, new BigDecimal("2000"));
        Rule rule = Rule.builder().operator(operator).operand(operand).build();

        // WHEN
        boolean result = ruleMatcher.matches(rule, signal);

        // THEN
        assertEquals(expectedResult, result);
    }
}