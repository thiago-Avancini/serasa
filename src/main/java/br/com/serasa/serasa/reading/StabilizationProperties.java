package br.com.serasa.serasa.reading;

import java.math.BigDecimal;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scale.stabilization")
public record StabilizationProperties(
        Duration windowDuration,
        int minSamples,
        BigDecimal toleranceKg,
        BigDecimal emptyThresholdKg,
        Duration staleAfter
) {

    public StabilizationProperties {
        windowDuration = windowDuration != null ? windowDuration : Duration.ofMillis(1000);
        minSamples = minSamples > 0 ? minSamples : 10;
        toleranceKg = toleranceKg != null ? toleranceKg : BigDecimal.valueOf(20);
        emptyThresholdKg = emptyThresholdKg != null ? emptyThresholdKg : BigDecimal.valueOf(100);
        staleAfter = staleAfter != null ? staleAfter : Duration.ofSeconds(3);
    }
}