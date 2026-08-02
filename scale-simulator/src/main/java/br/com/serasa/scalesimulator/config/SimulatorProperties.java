package br.com.serasa.scalesimulator.config;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "simulator")
public record SimulatorProperties(
        String targetBaseUrl,
        Duration readingInterval,
        Duration discoveryInterval,
        List<ScaleConfig> scales,
        WeightRange weight,
        SecondsRange plateau,
        SecondsRange idle
) {

    /**
     * One simulated ESP32 + truck pair. Holds everything needed to bootstrap the demo
     * cadastros (branch, grain type, truck, scale) through the real API before weighing.
     */
    public record ScaleConfig(
            String scaleCode,
            String branchCode,
            String branchName,
            String truckPlate,
            BigDecimal truckTareKg,
            String grainTypeName,
            BigDecimal grainPurchasePricePerTon,
            BigDecimal grainAvailableQuantityTons
    ) {
    }

    public record WeightRange(BigDecimal targetMinKg, BigDecimal targetMaxKg) {
    }

    public record SecondsRange(int minSeconds, int maxSeconds) {
    }
}