package com.edelmetalle.notifier.pricemonitor.controller;

import com.edelmetalle.notifier.pricemonitor.dto.MarketSignalDto;
import com.edelmetalle.notifier.pricemonitor.service.PriceMonitorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PriceController {

    private final PriceMonitorService priceMonitorService;

    @PostMapping("/new-price")
    public ResponseEntity<Void> receivePrice(@Valid @RequestBody MarketSignalDto signal) {
        priceMonitorService.processSignal(signal);
        return ResponseEntity.ok().build();
    }
}