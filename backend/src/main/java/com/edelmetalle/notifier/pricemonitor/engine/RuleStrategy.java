package com.edelmetalle.notifier.pricemonitor.engine;

import com.edelmetalle.notifier.pricemonitor.dto.MarketSignalDto;
import com.edelmetalle.notifier.pricemonitor.model.Rule;

public interface RuleStrategy {
    boolean supports(String operator);
    boolean evaluate(Rule rule, MarketSignalDto signal);
}
