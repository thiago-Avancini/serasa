package br.com.serasa.scalesimulator.client.dto;

public record ScaleCreateRequest(String code, Long branchId, String location, Boolean active) {
}