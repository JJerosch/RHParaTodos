package proj.paratodos.dto;

import proj.paratodos.domain.PontoMarcacao;

import java.time.LocalDateTime;

public record PontoMarcacaoResponse(
        Long id,
        String tipo,
        LocalDateTime dataHora,
        String origem
) {
    public static PontoMarcacaoResponse fromEntity(PontoMarcacao m) {
        return new PontoMarcacaoResponse(
                m.getId(),
                m.getTipo(),
                m.getDataHora(),
                m.getOrigem()
        );
    }
}