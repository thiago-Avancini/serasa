package br.com.serasa.serasa.graintype.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record GrainTypeRequest(
        @NotBlank @Size(max = 60) String name,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal purchasePricePerTon,
        @NotNull @DecimalMin(value = "0.0") BigDecimal availableQuantityTons
) {
}
