package proj.paratodos.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FeriasRequest(
        @NotNull Long funcionarioId,
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate dataFim,
        String observacao
) {
}