package com.edelmetalle.notifier.pricemonitor.engine;

import com.edelmetalle.notifier.pricemonitor.dto.MarketSignalDto;
import com.edelmetalle.notifier.pricemonitor.enums.RuleOperator;
import com.edelmetalle.notifier.pricemonitor.model.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PriceComparisonStrategyTest {

    private final PriceComparisonStrategy strategy = new PriceComparisonStrategy();

    @Test
    void shouldReturnTrueWhenPriceIsHigher() {
        Rule rule = Rule.builder()
                .operator(RuleOperator.PRICE_GREATER)
                .operand("2500.00")
                .build();

        MarketSignalDto signal = new MarketSignalDto("gold", new BigDecimal("2500.01"));

        assertTrue(strategy.evaluate(rule, signal));
    }

    @Test
    void shouldReturnFalseWhenPriceIsLower() {
        Rule rule = Rule.builder()
                .operator(RuleOperator.PRICE_GREATER)
                .operand("2500.00")
                .build();

        MarketSignalDto signal = new MarketSignalDto("gold", new BigDecimal("2499.99"));

        assertFalse(strategy.evaluate(rule, signal));
    }

    @Test
    void shouldReturnTrueWhenPricesAreEqual() {
        Rule rule = Rule.builder()
                .operator(RuleOperator.PRICE_EQUAL)
                .operand("2500.00")
                .build();
        MarketSignalDto signal = new MarketSignalDto("gold", new BigDecimal("2500.00"));

        assertTrue(strategy.evaluate(rule, signal));
    }

    @Test
    void shouldReturnTrueWhenPriceIsLessOrEqual() {
        Rule rule = Rule.builder()
                .operator(RuleOperator.PRICE_LESS_OR_EQUAL)
                .operand("2500.00")
                .build();

        MarketSignalDto signalEqual = new MarketSignalDto("gold", new BigDecimal("2500.00"));
        MarketSignalDto signalLess = new MarketSignalDto("gold", new BigDecimal("2400.00"));

        assertTrue(strategy.evaluate(rule, signalEqual));
        assertTrue(strategy.evaluate(rule, signalLess));
    }

    @Test
    void shouldHandleVeryLargePrices() {
        Rule rule = Rule.builder()
                .operator(RuleOperator.PRICE_GREATER)
                .operand("9999999999999.99")
                .build();
        MarketSignalDto signal = new MarketSignalDto("gold", new BigDecimal("10000000000000.00"));

        assertTrue(strategy.evaluate(rule, signal));
    }

    @Test
    void shouldHandleVerySmallPrices() {
        Rule rule = Rule.builder()
                .operator(RuleOperator.PRICE_LESS)
                .operand("0.01")
                .build();
        MarketSignalDto signal = new MarketSignalDto("gold", new BigDecimal("0.001"));

        assertTrue(strategy.evaluate(rule, signal));
    }

    @Test
    void shouldHandleExactDecimalPrecision() {
        Rule rule = Rule.builder()
                .operator(RuleOperator.PRICE_EQUAL)
                .operand("2500.50")
                .build();
        MarketSignalDto signal = new MarketSignalDto("gold", new BigDecimal("2500.50"));

        assertTrue(strategy.evaluate(rule, signal));
    }

    @Test
    void shouldHandleThreeDecimalPlaces() {
        Rule rule = Rule.builder()
                .operator(RuleOperator.PRICE_GREATER)
                .operand("2500.50")
                .build();
        MarketSignalDto signal = new MarketSignalDto("gold", new BigDecimal("2500.501"));

        assertTrue(strategy.evaluate(rule, signal));
    }

    @ParameterizedTest
    @CsvSource({
            "2000, 2000, true",
            "1999.99, 2000, true",
            "2000.01, 2000, false"
    })
    void shouldHandleLessThanOrEqualCorrectly(BigDecimal signalPrice, String operand, boolean expected) {
        Rule rule = Rule.builder()
                .operator(RuleOperator.PRICE_LESS_OR_EQUAL)
                .operand(operand)
                .build();
        MarketSignalDto signal = new MarketSignalDto("gold", signalPrice);

        assertEquals(expected, strategy.evaluate(rule, signal));
    }

    @Test
    void shouldReturnTrueForSupportsWithCorrectOperator() {
        assertTrue(strategy.supports("PRICE_EQUAL"));
        assertTrue(strategy.supports("PRICE_GREATER"));
        assertTrue(strategy.supports("PRICE_GREATER_OR_EQUAL"));
        assertTrue(strategy.supports("PRICE_LESS"));
        assertTrue(strategy.supports("PRICE_LESS_OR_EQUAL"));
    }

    @Test
    void shouldReturnFalseForSupportsWithIncorrectOperator() {
        assertFalse(strategy.supports("ITEM_IS"));
        assertFalse(strategy.supports("UNKNOWN"));
    }
}