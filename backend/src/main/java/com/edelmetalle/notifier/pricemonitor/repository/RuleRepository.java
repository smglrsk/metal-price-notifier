package com.edelmetalle.notifier.pricemonitor.repository;

import com.edelmetalle.notifier.pricemonitor.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleRepository extends JpaRepository<Rule, Long> {
}
