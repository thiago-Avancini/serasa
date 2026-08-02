package br.com.serasa.serasa.report.dto;

import java.math.BigDecimal;

public record GrainTypeReportItem(
        Long grainTypeId,
        String grainTypeName,
        long completedTransactions,
        BigDecimal totalNetWeightTons,
        BigDecimal totalCargoCost,
        BigDecimal availableQuantityTons,
        BigDecimal currentMarginPercentage,
        BigDecimal currentSalePricePerTon,
        BigDecimal potentialProfit
) {
}