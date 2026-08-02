package br.com.serasa.serasa.graintype;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pricing.margin")
public record MarginProperties(
        BigDecimal min,
        BigDecimal max,
        BigDecimal referenceQuantityTons
) {

    public MarginProperties {
        min = min != null ? min : new BigDecimal("0.05");
        max = max != null ? max : new BigDecimal("0.20");
        referenceQuantityTons = referenceQuantityTons != null && referenceQuantityTons.compareTo(BigDecimal.ZERO) > 0
                ? referenceQuantityTons
                : new BigDecimal("500");
    }
}