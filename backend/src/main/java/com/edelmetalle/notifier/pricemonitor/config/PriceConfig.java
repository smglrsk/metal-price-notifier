    package com.edelmetalle.notifier.pricemonitor.config;

    import lombok.Setter;
    import org.springframework.boot.context.properties.ConfigurationProperties;
    import org.springframework.stereotype.Component;

    import java.util.List;

    @Component
    @ConfigurationProperties(prefix = "app.prices")
    @Setter
    public class PriceConfig {
        private List<String> supportedItems;

        public List<String> getSupportedItems() {
            if (supportedItems == null) return List.of();

            return supportedItems.stream()
                    .map(String::toLowerCase)
                    .toList();
        }
    }