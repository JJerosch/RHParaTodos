package proj.paratodos.dto;

import proj.paratodos.domain.Solicitacao;

import java.time.LocalDateTime;

public record SolicitacaoResponse(
        Long id,
        String tipo,
        String tipoLabel,
        String status,
        String statusLabel,
        String referenciaTipo,
        Long referenciaId,
        String referenciaDescricao,
        Long solicitanteId,
        String solicitanteEmail,
        Long aprovadorId,
        String aprovadorEmail,
        String motivo,
        String observacao,
        String dadosAntes,
        String dadosDepois,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        LocalDateTime decididoEm
) {

    public static SolicitacaoResponse fromEntity(Solicitacao s) {
        return fromEntity(s, null);
    }

    public static SolicitacaoResponse fromEntity(Solicitacao s, String referenciaDescricao) {
        return new SolicitacaoResponse(
                s.getId(),
                s.getTipo() != null ? s.getTipo().name() : null,
                tipoToLabel(s.getTipo() != null ? s.getTipo().name() : null),
                s.getStatus() != null ? s.getStatus().name() : null,
                statusToLabel(s.getStatus() != null ? s.getStatus().name() : null),
                s.getReferenciaTipo() != null ? s.getReferenciaTipo().name() : null,
                s.getReferenciaId(),
                referenciaDescricao,
                s.getSolicitante() != null ? s.getSolicitante().getId() : null,
                s.getSolicitante() != null ? s.getSolicitante().getEmail() : null,
                s.getAprovador() != null ? s.getAprovador().getId() : null,
                s.getAprovador() != null ? s.getAprovador().getEmail() : null,
                s.getMotivo(),
                s.getObservacao(),
                s.getDadosAntes(),
                s.getDadosDepois(),
                s.getCriadoEm(),
                s.getAtualizadoEm(),
                s.getDecididoEm()
        );
    }

    private static String tipoToLabel(String tipo) {
        if (tipo == null) return "";
        return switch (tipo) {
            case "ALTERACAO_FUNCIONARIO"   -> "Alteração de Funcionário";
            case "ATIVACAO_FUNCIONARIO"    -> "Ativação de Funcionário";
            case "DESATIVACAO_FUNCIONARIO" -> "Desativação de Funcionário";
            case "EXCLUSAO_FUNCIONARIO"    -> "Exclusão de Funcionário";
            case "CRIACAO_DEPARTAMENTO"    -> "Criação de Departamento";
            case "EDICAO_DEPARTAMENTO"     -> "Edição de Departamento";
            case "DESATIVACAO_DEPARTAMENTO"-> "Desativação de Departamento";
            case "EXCLUSAO_DEPARTAMENTO"   -> "Exclusão de Departamento";
            case "CRIACAO_CARGO"           -> "Criação de Cargo";
            case "EDICAO_CARGO"            -> "Edição de Cargo";
            case "DESATIVACAO_CARGO"       -> "Desativação de Cargo";
            case "EXCLUSAO_CARGO"          -> "Exclusão de Cargo";
            case "ABERTURA_VAGA"           -> "Abertura de Vaga";
            case "PROMOCAO"                -> "Promoção";
            case "TRANSFERENCIA"           -> "Transferência";
            case "REAJUSTE_SALARIAL"       -> "Reajuste Salarial";
            default -> tipo;
        };
    }

    private static String statusToLabel(String status) {
        if (status == null) return "";
        return switch (status) {
            case "PENDENTE"  -> "Pendente";
            case "APROVADA"  -> "Aprovada";
            case "REJEITADA" -> "Rejeitada";
            case "CANCELADA" -> "Cancelada";
            default -> status;
        };
    }
}
