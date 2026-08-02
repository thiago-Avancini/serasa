package br.com.serasa.serasa.transport.dto;

import br.com.serasa.serasa.transport.TransactionStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record TransportTransactionResponse(
        Long id,
        String truckPlate,
        String grainTypeName,
        String originBranchName,
        String scaleCode,
        TransactionStatus status,
        Instant startedAt,
        Instant weighingStartedAt,
        Instant weighingCompletedAt,
        BigDecimal grossWeightKg,
        BigDecimal tareWeightKg,
        BigDecimal netWeightKg,
        BigDecimal purchasePriceSnapshot,
        BigDecimal cargoCost
) {
}
