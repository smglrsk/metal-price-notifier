package com.edelmetalle.notifier.pricemonitor;

import com.edelmetalle.notifier.pricemonitor.dto.MarketSignalDto;
import com.edelmetalle.notifier.pricemonitor.service.NotificationService;
import com.edelmetalle.notifier.pricemonitor.service.PriceMonitorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class PriceMonitorBankExampleTest {

    @Autowired
    private PriceMonitorService priceMonitorService;

    @MockitoSpyBean
    private NotificationService notificationService;

    @Test
    void shouldSendExactly4NotificationsForSilverPrice56_54() {
        // Given
        MarketSignalDto signal = new MarketSignalDto("silver", new BigDecimal("56.54"));

        // When
        priceMonitorService.processSignal(signal);

        // Then
        verify(notificationService, times(1)).sendNotification(
                eq("janina@gdziestam.com"),
                eq("Srebro rekordowo tanie od roku"),
                eq("Srebro najtańsze od roku!")
        );

        verify(notificationService, times(1)).sendNotification(
                eq("grazyna@gdziestam.com"),
                eq("Srebro rekordowo tanie od roku"),
                eq("Srebro najtańsze od roku!")
        );

        verify(notificationService, times(1)).sendNotification(
                eq("ktos@gdziestam.com"),
                eq("Srebro niebezpiecznie tanie"),
                eq("Uwaga! Srebro bardzo tanie!")
        );

        verify(notificationService, times(1)).sendNotification(
                eq("nikt@gdziestam.com"),
                eq("Srebro niebezpiecznie tanie"),
                eq("Uwaga! Srebro bardzo tanie!")
        );

        verify(notificationService, times(4)).sendNotification(anyString(), anyString(), anyString());
    }
}