package br.com.serasa.serasa.report.dto;

import java.math.BigDecimal;

public record BranchReportItem(
        Long branchId,
        String branchName,
        long completedTransactions,
        BigDecimal totalNetWeightTons,
        BigDecimal totalCargoCost
) {
}