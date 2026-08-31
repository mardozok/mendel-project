package ar.com.bds.model.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

public record TransactionInputDTO(
        Long id,
        @NotNull @PositiveOrZero Double amount,
        @NotBlank String type,
        Long parentId
) {
}
