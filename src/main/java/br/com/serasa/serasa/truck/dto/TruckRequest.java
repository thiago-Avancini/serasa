package br.com.serasa.serasa.truck.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record TruckRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{3}-?\\d[A-Za-z0-9]\\d{2}$", message = "invalid plate format")
        String plate,

        @NotNull @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal tareWeightKg,

        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal cargoCapacityTons
) {
}
