package br.com.serasa.serasa.graintype.dto;

import java.math.BigDecimal;

public record GrainTypeResponse(
        Long id,
        String name,
        BigDecimal purchasePricePerTon,
        BigDecimal availableQuantityTons,
        BigDecimal currentMarginPercentage,
        BigDecimal currentSalePricePerTon
) {
}