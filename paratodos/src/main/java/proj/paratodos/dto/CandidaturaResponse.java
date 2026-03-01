package proj.paratodos.dto;

import proj.paratodos.domain.Candidatura;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CandidaturaResponse(
        Long id,
        Long vagaId,
        String vagaTitulo,
        Long candidatoId,
        String candidatoNome,
        String candidatoEmail,
        String candidatoTelefone,
        String etapa,
        BigDecimal notaGeral,
        String observacoes,
        String motivoRejeicao,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {

    public static CandidaturaResponse fromEntity(Candidatura c) {
        return new CandidaturaResponse(
                c.getId(),
                c.getVaga() != null ? c.getVaga().getId() : null,
                c.getVaga() != null ? c.getVaga().getTitulo() : null,
                c.getCandidato() != null ? c.getCandidato().getId() : null,
                c.getCandidato() != null ? c.getCandidato().getNomeCompleto() : null,
                c.getCandidato() != null ? c.getCandidato().getEmail() : null,
                c.getCandidato() != null ? c.getCandidato().getTelefone() : null,
                c.getEtapa(),
                c.getNotaGeral(),
                c.getObservacoes(),
                c.getMotivoRejeicao(),
                c.getCriadoEm(),
                c.getAtualizadoEm()
        );
    }
}
