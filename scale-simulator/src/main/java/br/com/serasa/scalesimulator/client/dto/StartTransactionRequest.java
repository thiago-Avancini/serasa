package br.com.serasa.scalesimulator.client.dto;

public record StartTransactionRequest(Long truckId, Long grainTypeId, Long originBranchId) {
}