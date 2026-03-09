package proj.paratodos.dto;

import jakarta.validation.constraints.NotBlank;

public record PontoCalendarioUpdateRequest(
        @NotBlank String tipo,
        String descricao
) {
}