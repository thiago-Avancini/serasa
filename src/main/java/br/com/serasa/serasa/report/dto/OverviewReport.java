package br.com.serasa.serasa.report.dto;

import java.math.BigDecimal;
import java.util.Map;
import br.com.serasa.serasa.transport.TransactionStatus;

public record OverviewReport(
        long totalTransactions,
        Map<TransactionStatus, Long> transactionsByStatus,
        BigDecimal totalCargoCost,
        BigDecimal totalNetWeightTons
) {
}