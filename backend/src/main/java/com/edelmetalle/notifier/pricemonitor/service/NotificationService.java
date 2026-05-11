package com.edelmetalle.notifier.pricemonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendNotification(String recipient, String title, String content) {
        log.info("--------------------------------------------------");
        log.info("WYSYŁKA POWIADOMIENIA (MOCK):");
        log.info("Odbiorca: {}", recipient);
        log.info("Tytuł:    {}", title);
        log.info("Treść:    {}", content);
        log.info("--------------------------------------------------");
    }
}