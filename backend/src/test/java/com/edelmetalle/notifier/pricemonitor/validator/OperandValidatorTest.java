package com.edelmetalle.notifier.pricemonitor.validator;

import com.edelmetalle.notifier.pricemonitor.config.PriceConfig;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperandValidatorTest {

    @Mock
    private PriceConfig priceConfig;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @InjectMocks
    private OperandValidator validator;

    @BeforeEach

    @Test
    void shouldAcceptValidNumericValues() {

        assertTrue(validator.isValid("2500", null));
        assertTrue(validator.isValid("2500.50", null));
    }

    @Test
    void shouldAcceptSupportedItemsIgnoringCase() {

        when(priceConfig.getSupportedItems()).thenReturn(List.of("gold", "silver"));

        assertTrue(validator.isValid("gold", null));
        assertTrue(validator.isValid("GOLD", null));
    }

    @Test
    void shouldRejectInvalidValues() {

        when(priceConfig.getSupportedItems()).thenReturn(List.of("gold"));


        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);

        assertFalse(validator.isValid("pizza", context));
        assertFalse(validator.isValid("  ", context));
        assertFalse(validator.isValid(null, context));

        verify(context, atLeastOnce()).disableDefaultConstraintViolation();
    }
}