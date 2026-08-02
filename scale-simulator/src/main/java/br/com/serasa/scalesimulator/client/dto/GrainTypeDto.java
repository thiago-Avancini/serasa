package br.com.serasa.scalesimulator.client.dto;

import java.math.BigDecimal;

public record GrainTypeDto(
        Long id,
        String name,
        BigDecimal purchasePricePerTon,
        BigDecimal availableQuantityTons,
        BigDecimal currentMarginPercentage,
        BigDecimal currentSalePricePerTon
) {
}