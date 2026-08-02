package br.com.serasa.serasa.branch.dto;

public record BranchResponse(
        Long id,
        String code,
        String name,
        String city,
        String state
) {
}
