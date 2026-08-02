package br.com.serasa.serasa.scale.dto;

public record ScaleResponse(
        String code,
        Long branchId,
        String branchName,
        String location,
        boolean active
) {
}
