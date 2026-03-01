package proj.paratodos.dto;

import proj.paratodos.domain.Promocao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromocaoResponse(
        Long id,
        Long funcionarioId,
        String funcionarioNome,
        String funcionarioMatricula,
        String funcionarioEmail,
        Long cargoAtualId,
        String cargoAtualTitulo,
        Long cargoNovoId,
        String cargoNovoTitulo,
        Long departamentoAtualId,
        String departamentoAtualNome,
        Long departamentoNovoId,
        String departamentoNovoNome,
        BigDecimal salarioAtual,
        BigDecimal salarioNovo,
        String motivo,
        String tipo,
        Long solicitanteId,
        String solicitanteEmail,
        Long aprovadorId,
        String aprovadorEmail,
        String status,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataDecisao,
        String observacaoAprovador
) {

    public static PromocaoResponse fromEntity(Promocao p) {
        return new PromocaoResponse(
                p.getId(),
                p.getFuncionario() != null ? p.getFuncionario().getId() : null,
                p.getFuncionario() != null ? p.getFuncionario().getNomeCompleto() : null,
                p.getFuncionario() != null ? p.getFuncionario().getMatricula() : null,
                p.getFuncionario() != null ? p.getFuncionario().getEmailCorporativo() : null,
                p.getCargoAtual() != null ? p.getCargoAtual().getId() : null,
                p.getCargoAtual() != null ? p.getCargoAtual().getTitulo() : null,
                p.getCargoNovo() != null ? p.getCargoNovo().getId() : null,
                p.getCargoNovo() != null ? p.getCargoNovo().getTitulo() : null,
                p.getDepartamentoAtual() != null ? p.getDepartamentoAtual().getId() : null,
                p.getDepartamentoAtual() != null ? p.getDepartamentoAtual().getNome() : null,
                p.getDepartamentoNovo() != null ? p.getDepartamentoNovo().getId() : null,
                p.getDepartamentoNovo() != null ? p.getDepartamentoNovo().getNome() : null,
                p.getSalarioAtual(),
                p.getSalarioNovo(),
                p.getMotivo(),
                p.getTipo(),
                p.getSolicitante() != null ? p.getSolicitante().getId() : null,
                p.getSolicitante() != null ? p.getSolicitante().getEmail() : null,
                p.getAprovador() != null ? p.getAprovador().getId() : null,
                p.getAprovador() != null ? p.getAprovador().getEmail() : null,
                p.getStatus(),
                p.getDataSolicitacao(),
                p.getDataDecisao(),
                p.getObservacaoAprovador()
        );
    }
}
