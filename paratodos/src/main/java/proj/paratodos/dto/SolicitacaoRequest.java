package proj.paratodos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitacaoRequest(

        @NotNull(message = "Tipo é obrigatório")
        String tipo,

        String referenciaTipo,

        Long referenciaId,

        @NotBlank(message = "Motivo é obrigatório")
        String motivo,

        String dadosAntes,

        String dadosDepois
) {}
