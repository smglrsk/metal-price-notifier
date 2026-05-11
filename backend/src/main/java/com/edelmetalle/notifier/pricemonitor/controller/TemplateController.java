package com.edelmetalle.notifier.pricemonitor.controller;

import com.edelmetalle.notifier.pricemonitor.config.PriceConfig;
import com.edelmetalle.notifier.pricemonitor.dto.NotificationTemplateDto;
import com.edelmetalle.notifier.pricemonitor.dto.NotificationTemplateResponseDto;
import com.edelmetalle.notifier.pricemonitor.model.NotificationTemplate;
import com.edelmetalle.notifier.pricemonitor.service.TemplateManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@Slf4j
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateManagementService templateService;
    private final PriceConfig priceConfig;

    @GetMapping
    public Page<NotificationTemplateResponseDto> getAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<NotificationTemplate> entities = templateService.getAll(search, pageable);

        // ZWROT: DTO zamiast encji
        return entities.map(templateService::convertToResponseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationTemplateResponseDto> getById(@PathVariable Long id) {
        NotificationTemplate template = templateService.getById(id);
        return ResponseEntity.ok(templateService.convertToResponseDto(template));
    }

    @PostMapping
    public ResponseEntity<NotificationTemplateResponseDto> save(@Valid @RequestBody NotificationTemplateDto dto) {
        NotificationTemplate saved = templateService.createFromDto(dto);
        return ResponseEntity.ok(templateService.convertToResponseDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationTemplateResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody NotificationTemplateDto dto) {

        NotificationTemplate updated = templateService.updateFromDto(id, dto);
        return ResponseEntity.ok(templateService.convertToResponseDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/supported-items")
    public List<String> getSupportedItems() {
        return priceConfig.getSupportedItems();
    }
}