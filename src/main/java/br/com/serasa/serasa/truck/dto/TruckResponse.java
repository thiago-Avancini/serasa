package br.com.serasa.serasa.truck.dto;

import java.math.BigDecimal;

public record TruckResponse(
        Long id,
        String plate,
        BigDecimal tareWeightKg,
        BigDecimal cargoCapacityTons
) {
}
