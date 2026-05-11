package com.edelmetalle.notifier.pricemonitor.repository;

import com.edelmetalle.notifier.pricemonitor.model.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

    public interface RecipientRepository extends JpaRepository<Recipient, Long> {
}
