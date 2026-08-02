package br.com.serasa.serasa.report.dto;

import java.math.BigDecimal;

public record ScaleReportItem(
        String scaleCode,
        String branchName,
        long completedWeighings,
        BigDecimal averageStabilizationSeconds
) {
}