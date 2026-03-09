package proj.paratodos.dto;

import proj.paratodos.domain.PontoOcorrencia;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PontoOcorrenciaResponse(
        Long id,
        Long funcionarioId,
        String funcionarioNome,
        LocalDate dataInicio,
        LocalDate dataFim,
        String tipo,
        String observacao,
        Boolean abonaDia,
        Boolean bloqueiaMarcacao,
        LocalDateTime criadoEm
) {
    public static PontoOcorrenciaResponse fromEntity(PontoOcorrencia o) {
        return new PontoOcorrenciaResponse(
                o.getId(),
                o.getFuncionario().getId(),
                o.getFuncionario().getNomeCompleto(),
                o.getDataInicio(),
                o.getDataFim(),
                o.getTipo(),
                o.getObservacao(),
                o.getAbonaDia(),
                o.getBloqueiaMarcacao(),
                o.getCriadoEm()
        );
    }
}