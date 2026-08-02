package br.com.serasa.serasa.branch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BranchRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 80) String city,
        @Size(min = 2, max = 2) String state
) {
}
