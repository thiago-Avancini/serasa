package br.com.serasa.serasa.scale.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ScaleRequest(
        @NotBlank @Size(max = 30) String code,
        @NotNull Long branchId,
        @Size(max = 120) String location,
        Boolean active
) {
}
