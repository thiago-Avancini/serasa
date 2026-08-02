package br.com.serasa.serasa.reading.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Mirrors the exact JSON the ESP32 sends: {"id", "plate", "weight"}.
 */
public record ScaleReadingRequest(
        @NotBlank String id,
        @NotBlank String plate,
        @NotNull @DecimalMin(value = "0.0") BigDecimal weight
) {
}
