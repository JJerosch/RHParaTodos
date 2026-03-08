package proj.paratodos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitacaoRequest(
        @NotBlank String tipo, // EDICAO, DESATIVACAO, EXCLUSAO
        @NotNull Long funcionarioId,
        @NotBlank String motivo,
        String dadosJson // JSON com dados propostos (para EDICAO)
) {}
