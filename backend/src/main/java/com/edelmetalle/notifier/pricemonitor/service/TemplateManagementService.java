package com.edelmetalle.notifier.pricemonitor.service;

import com.edelmetalle.notifier.pricemonitor.config.PriceConfig;
import com.edelmetalle.notifier.pricemonitor.dto.NotificationTemplateDto;
import com.edelmetalle.notifier.pricemonitor.dto.NotificationTemplateResponseDto;
import com.edelmetalle.notifier.pricemonitor.enums.RuleOperator;
import com.edelmetalle.notifier.pricemonitor.model.NotificationTemplate;
import com.edelmetalle.notifier.pricemonitor.model.Recipient;
import com.edelmetalle.notifier.pricemonitor.model.Rule;
import com.edelmetalle.notifier.pricemonitor.repository.NotificationTemplateRepository;
import com.edelmetalle.notifier.pricemonitor.repository.RecipientRepository;
import com.edelmetalle.notifier.pricemonitor.repository.RuleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TemplateManagementService {

    private final NotificationTemplateRepository templateRepository;
    private final RuleRepository ruleRepository;
    private final RecipientRepository recipientRepository;
    private final PriceConfig priceConfig;

    @Transactional(readOnly = true)
    public Page<NotificationTemplate> getAll(String search, Pageable pageable) {
        log.info("Pobieranie szablonów. Fraza wyszukiwania: '{}', Strona: {}", search, pageable.getPageNumber());

        Pageable sortedByTitle = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("title").ascending()
        );

        if (search != null && !search.isBlank()) {
            return templateRepository.findByTitleContainingIgnoreCase(search, sortedByTitle);
        }
        return templateRepository   .findAll(sortedByTitle);
    }

    @Transactional(readOnly = true)
    public NotificationTemplate getById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Szablon o ID " + id + " nie istnieje"));
    }

    public NotificationTemplate save(NotificationTemplate template) {
        return templateRepository.save(template);
    }

    public void delete(Long id) {
        log.warn("Usuwanie szablonu o ID: {}", id);

        if (!templateRepository.existsById(id)) {
            throw new EntityNotFoundException("Szablon o ID " + id + " nie istnieje");
        }

        templateRepository.deleteById(id);
    }

    public NotificationTemplate createFromDto(NotificationTemplateDto dto) {
        log.info("Tworzenie nowego szablonu: {}", dto.title());

        validateRules(dto.rules());

        validateRecipients(dto.recipients());

        NotificationTemplate template = NotificationTemplate.builder()
                .title(dto.title())
                .content(dto.content())
                .rules(new HashSet<>())
                .recipients(new HashSet<>())
                .build();

        if (dto.rules() != null) {
            dto.rules().forEach(ruleDto -> {
                Rule rule = Rule.builder()
                        .operator(RuleOperator.valueOf(ruleDto.operator()))
                        .operand(ruleDto.operand())
                        .template(template)
                        .build();
                template.getRules().add(rule);
            });
        }

        if (dto.recipients() != null) {

            Set<String> uniqueEmails = dto.recipients().stream()
                    .map(NotificationTemplateDto.RecipientDto::email)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            uniqueEmails.forEach(email -> {
                Recipient recipient = Recipient.builder()
                        .email(email)
                        .template(template)
                        .build();
                template.getRecipients().add(recipient);
            });
        }

        return templateRepository.save(template);
    }

    public NotificationTemplate updateFromDto(Long id, NotificationTemplateDto dto) {
        NotificationTemplate existing = getById(id);

        log.info("Aktualizacja szablonu ID {}: {}", id, dto.title());


        validateRules(dto.rules());
        validateRecipients(dto.recipients());

        existing.setTitle(dto.title());
        existing.setContent(dto.content());

        updateRules(existing, dto.rules());

        updateRecipients(existing, dto.recipients());

        return templateRepository.save(existing);
    }

    private void updateRules(NotificationTemplate template, java.util.List<NotificationTemplateDto.RuleDto> newRulesDto) {

        Set<Rule> toRemove = template.getRules().stream()
                .filter(rule -> newRulesDto.stream()
                        .noneMatch(r -> r.id() != null && r.id().equals(rule.getId())))
                .collect(Collectors.toSet());

        toRemove.forEach(rule -> {
            template.getRules().remove(rule);
            ruleRepository.delete(rule);
        });

        newRulesDto.forEach(ruleDto -> {
            if (ruleDto.id() != null) {

                template.getRules().stream()
                        .filter(r -> r.getId().equals(ruleDto.id()))
                        .findFirst()
                        .ifPresent(rule -> {
                            rule.setOperator(RuleOperator.valueOf(ruleDto.operator()));
                            rule.setOperand(ruleDto.operand());
                        });
            } else {

                Rule newRule = Rule.builder()
                        .operator(RuleOperator.valueOf(ruleDto.operator()))
                        .operand(ruleDto.operand())
                        .template(template)
                        .build();
                template.getRules().add(newRule);
            }
        });
    }

    private void updateRecipients(NotificationTemplate template, java.util.List<NotificationTemplateDto.RecipientDto> newRecipientsDto) {
        Set<String> newEmails = newRecipientsDto.stream()
                .map(NotificationTemplateDto.RecipientDto::email)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<Recipient> toRemove = template.getRecipients().stream()
                .filter(recipient -> !newEmails.contains(recipient.getEmail().toLowerCase()))
                .collect(Collectors.toSet());

        toRemove.forEach(recipient -> {
            template.getRecipients().remove(recipient);
            recipientRepository.delete(recipient);
        });

        Set<String> existingEmails = template.getRecipients().stream()
                .map(Recipient::getEmail)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        newEmails.stream()
                .filter(email -> !existingEmails.contains(email))
                .forEach(email -> {
                    Recipient newRecipient = Recipient.builder()
                            .email(email)
                            .template(template)
                            .build();
                    template.getRecipients().add(newRecipient);
                });
    }

    private void validateRules(java.util.List<NotificationTemplateDto.RuleDto> rules) {
        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException("Szablon musi mieć co najmniej jedną regułę");
        }

        for (var ruleDto : rules) {
            try {
                RuleOperator operator = RuleOperator.valueOf(ruleDto.operator());
                String operand = ruleDto.operand();

                if (operator == RuleOperator.ITEM_IS || operator == RuleOperator.ITEM_IS_NOT) {
                    List<String> supported = priceConfig.getSupportedItems();
                    String pattern = String.join("|", supported);

                    if (!operand.matches("(?i)^(" + pattern + ")$")) {
                        throw new IllegalArgumentException(
                                "Dla operatora " + operator.getDescription() +
                                        " operand musi być: gold, silver lub platinum (otrzymano: " + operand + ")"
                        );
                    }
                }

                else if (operator.name().startsWith("PRICE_")) {

                    if (!operand.matches("^\\d+(\\.\\d{1,2})?$")) {
                        throw new IllegalArgumentException(
                                "Dla operatora " + operator.getDescription() +
                                        " operand musi być liczbą z maksymalnie 2 miejscami po przecinku (otrzymano: " + operand + ")"
                        );
                    }

                    BigDecimal value = new BigDecimal(operand);
                    if (value.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("Cena nie może być ujemna: " + operand);
                    }
                }
            } catch (IllegalArgumentException e) {
                throw e; // Rzuć dalej
            } catch (Exception e) {
                throw new IllegalArgumentException("Niepoprawny operator: " + ruleDto.operator(), e);
            }
        }
    }

    private void validateRecipients(java.util.List<NotificationTemplateDto.RecipientDto> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("Szablon musi mieć co najmniej jednego odbiorcę");
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        for (var recipient : recipients) {
            if (recipient.email() == null || !recipient.email().matches(emailRegex)) {
                throw new IllegalArgumentException("Niepoprawny format email: " + recipient.email());
            }
        }
    }

    public NotificationTemplateResponseDto convertToResponseDto(NotificationTemplate template) {
        return new NotificationTemplateResponseDto(
                template.getId(),
                template.getTitle(),
                template.getContent(),
                template.getRules().stream()
                        .map(r -> new NotificationTemplateResponseDto.RuleDto(
                                r.getId(),
                                r.getOperator().name(),
                                r.getOperand()))
                        .toList(),
                template.getRecipients().stream()
                        .map(rec -> new NotificationTemplateResponseDto.RecipientDto(
                                rec.getId(),
                                rec.getEmail()))
                        .toList()
        );
    }

    public NotificationTemplate convertToEntity(NotificationTemplateDto dto) {
        NotificationTemplate template = NotificationTemplate.builder()
                .title(dto.title())
                .content(dto.content())
                .build();

        if (dto.rules() != null) {
            dto.rules().forEach(ruleDto -> {
                Rule rule = Rule.builder()
                        .operator(RuleOperator.valueOf(ruleDto.operator()))
                        .operand(ruleDto.operand())
                        .template(template)
                        .build();
                template.addRule(rule);
            });
        }

        if (dto.recipients() != null) {
            dto.recipients().forEach(recipientDto -> {
                Recipient recipient = Recipient.builder()
                        .email(recipientDto.email())
                        .template(template)
                        .build();
                template.addRecipient(recipient);
            });
        }

        return template;
    }
}