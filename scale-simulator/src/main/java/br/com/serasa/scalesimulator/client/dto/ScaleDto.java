package br.com.serasa.scalesimulator.client.dto;

public record ScaleDto(String code, Long branchId, String branchName, String location, boolean active) {
}