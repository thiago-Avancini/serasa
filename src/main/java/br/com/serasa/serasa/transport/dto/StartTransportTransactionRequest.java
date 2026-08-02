package br.com.serasa.serasa.transport.dto;

import jakarta.validation.constraints.NotNull;

public record StartTransportTransactionRequest(
        @NotNull Long truckId,
        @NotNull Long grainTypeId,
        @NotNull Long originBranchId
) {
}
