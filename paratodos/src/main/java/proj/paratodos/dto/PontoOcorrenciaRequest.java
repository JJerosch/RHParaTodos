package proj.paratodos.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PontoOcorrenciaRequest(
        @NotNull Long funcionarioId,
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate dataFim,
        @NotNull String tipo,
        String observacao,
        Boolean abonaDia,
        Boolean bloqueiaMarcacao
) {
}