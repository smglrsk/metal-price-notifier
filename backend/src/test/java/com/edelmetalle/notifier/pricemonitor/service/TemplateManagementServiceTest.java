package com.edelmetalle.notifier.pricemonitor.service;

import com.edelmetalle.notifier.pricemonitor.dto.NotificationTemplateDto;
import com.edelmetalle.notifier.pricemonitor.model.NotificationTemplate;
import com.edelmetalle.notifier.pricemonitor.repository.NotificationTemplateRepository;
import com.edelmetalle.notifier.pricemonitor.repository.RecipientRepository;
import com.edelmetalle.notifier.pricemonitor.repository.RuleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateManagementServiceTest {

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private RecipientRepository recipientRepository;

    @InjectMocks
    private TemplateManagementService service;

    private NotificationTemplateDto.RuleDto validRuleDto;
    private NotificationTemplateDto.RecipientDto validRecipientDto;

    @BeforeEach
    void setUp() {
        validRuleDto = new NotificationTemplateDto.RuleDto(null, "PRICE_GREATER", "2000");
        validRecipientDto = new NotificationTemplateDto.RecipientDto(null, "test@test.com");
    }

    @Test
    void shouldCreateTemplateFromDto() {
        // Given
        var dto = new NotificationTemplateDto(
                null,
                "Title",
                "Content",
                List.of(validRuleDto),
                List.of(validRecipientDto)
        );

        when(templateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationTemplate result = service.createFromDto(dto);

        // Then
        assertNotNull(result);
        assertEquals("Title", result.getTitle());
        assertEquals("Content", result.getContent());
        assertEquals(1, result.getRules().size());
        assertEquals(1, result.getRecipients().size());
        assertTrue(result.getRecipients().stream()
                .anyMatch(r -> r.getEmail().equals("test@test.com")));
        verify(templateRepository).save(any());
    }

    @Test
    void shouldDeduplicateEmailsWhenCreatingTemplate() {
        // Given
        var recipient1 = new NotificationTemplateDto.RecipientDto(null, "test@test.com");
        var recipient2 = new NotificationTemplateDto.RecipientDto(null, "TEST@test.com"); // duplikat
        var recipient3 = new NotificationTemplateDto.RecipientDto(null, "other@test.com");

        var dto = new NotificationTemplateDto(
                null,
                "Title",
                "Content",
                List.of(validRuleDto),
                List.of(recipient1, recipient2, recipient3)
        );

        when(templateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationTemplate result = service.createFromDto(dto);

        // Then
        assertEquals(2, result.getRecipients().size(), "Powinny być tylko 2 unikalne emaile");
        assertTrue(result.getRecipients().stream()
                .anyMatch(r -> r.getEmail().equalsIgnoreCase("test@test.com")));
        assertTrue(result.getRecipients().stream()
                .anyMatch(r -> r.getEmail().equals("other@test.com")));
    }

    @Test
    void shouldThrowExceptionWhenPriceOperatorHasItemOperand() {
        // Given
        var invalidRule = new NotificationTemplateDto.RuleDto(null, "PRICE_GREATER", "gold");
        var dto = new NotificationTemplateDto(
                null,
                "Title",
                "Content",
                List.of(invalidRule),
                List.of(validRecipientDto)
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createFromDto(dto));
        assertTrue(exception.getMessage().contains("operand musi być liczbą"));
    }

    @Test
    void shouldThrowExceptionWhenTemplateHasNoRules() {
        // Given
        var dto = new NotificationTemplateDto(
                null,
                "Title",
                "Content",
                List.of(), // Empty rules
                List.of(validRecipientDto)
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createFromDto(dto));
        assertEquals("Szablon musi mieć co najmniej jedną regułę", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTemplateHasNoRecipients() {
        // Given
        var dto = new NotificationTemplateDto(
                null,
                "Title",
                "Content",
                List.of(validRuleDto),
                List.of() // Empty recipients
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createFromDto(dto));
        assertEquals("Szablon musi mieć co najmniej jednego odbiorcę", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForInvalidEmail() {
        // Given
        var invalidRecipient = new NotificationTemplateDto.RecipientDto(null, "invalid-email");
        var dto = new NotificationTemplateDto(
                null,
                "Title",
                "Content",
                List.of(validRuleDto),
                List.of(invalidRecipient)
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createFromDto(dto));
        assertTrue(exception.getMessage().contains("Niepoprawny format email"));
    }

    @Test
    void shouldUpdateExistingTemplate() {
        // Given
        Long id = 1L;
        NotificationTemplate existing = NotificationTemplate.builder()
                .id(id)
                .title("Old")
                .content("Old Content")
                .build();

        var dto = new NotificationTemplateDto(
                id,
                "New Title",
                "New Content",
                List.of(validRuleDto),
                List.of(validRecipientDto)
        );

        when(templateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(templateRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        NotificationTemplate updated = service.updateFromDto(id, dto);

        // Then
        assertEquals("New Title", updated.getTitle());
        assertEquals("New Content", updated.getContent());
        verify(templateRepository).save(existing);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingTemplate() {
        // Given
        Long id = 999L;
        var dto = new NotificationTemplateDto(
                id,
                "Title",
                "Content",
                List.of(validRuleDto),
                List.of(validRecipientDto)
        );

        when(templateRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> service.updateFromDto(id, dto));
    }

    @Test
    void shouldGetById() {
        // Given
        Long id = 1L;
        NotificationTemplate expected = NotificationTemplate.builder()
                .id(id)
                .title("Test")
                .build();

        when(templateRepository.findById(id)).thenReturn(Optional.of(expected));

        // When
        NotificationTemplate result = service.getById(id);

        // Then
        assertNotNull(result);
        assertEquals("Test", result.getTitle());
    }

    @Test
    void shouldThrowExceptionWhenGetByIdNotFound() {
        // Given
        Long id = 999L;
        when(templateRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> service.getById(id));
    }

    @Test
    void shouldDeleteTemplate() {
        // Given
        Long id = 1L;
        when(templateRepository.existsById(id)).thenReturn(true);

        // When
        service.delete(id);

        // Then
        verify(templateRepository).deleteById(id);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingTemplate() {
        // Given
        Long id = 999L;
        when(templateRepository.existsById(id)).thenReturn(false);

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> service.delete(id));
        verify(templateRepository, never()).deleteById(any());
    }
}