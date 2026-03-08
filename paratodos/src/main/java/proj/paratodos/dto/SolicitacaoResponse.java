package proj.paratodos.dto;

import proj.paratodos.domain.Solicitacao;

import java.time.LocalDateTime;

public record SolicitacaoResponse(
        Long id,
        String tipo,
        Long funcionarioId,
        String funcionarioNome,
        String funcionarioMatricula,
        String solicitanteEmail,
        String dadosJson,
        String motivo,
        String status,
        String aprovadorEmail,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataDecisao,
        String observacaoAprovador
) {
    public static SolicitacaoResponse fromEntity(Solicitacao s) {
        return new SolicitacaoResponse(
                s.getId(),
                s.getTipo(),
                s.getFuncionario().getId(),
                s.getFuncionario().getNomeCompleto(),
                s.getFuncionario().getMatricula(),
                s.getSolicitante().getEmail(),
                s.getDadosJson(),
                s.getMotivo(),
                s.getStatus(),
                s.getAprovador() != null ? s.getAprovador().getEmail() : null,
                s.getDataSolicitacao(),
                s.getDataDecisao(),
                s.getObservacaoAprovador()
        );
    }
}
