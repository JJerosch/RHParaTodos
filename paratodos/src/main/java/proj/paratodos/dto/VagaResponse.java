package proj.paratodos.dto;

import proj.paratodos.domain.Vaga;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VagaResponse(
        Long id,
        String titulo,
        String descricao,
        Long departamentoId,
        String departamentoNome,
        Long cargoId,
        String cargoTitulo,
        Integer quantidade,
        String prioridade,
        BigDecimal salarioMin,
        BigDecimal salarioMax,
        String tipoContrato,
        String localTrabalho,
        String modeloTrabalho,
        String requisitos,
        String status,
        LocalDateTime publicadaEm,
        LocalDateTime encerradaEm,
        Long criadoPorId,
        String criadoPorEmail,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {

    public static VagaResponse fromEntity(Vaga v) {
        return new VagaResponse(
                v.getId(),
                v.getTitulo(),
                v.getDescricao(),
                v.getDepartamento() != null ? v.getDepartamento().getId() : null,
                v.getDepartamento() != null ? v.getDepartamento().getNome() : null,
                v.getCargo() != null ? v.getCargo().getId() : null,
                v.getCargo() != null ? v.getCargo().getTitulo() : null,
                v.getQuantidade(),
                v.getPrioridade(),
                v.getSalarioMin(),
                v.getSalarioMax(),
                v.getTipoContrato(),
                v.getLocalTrabalho(),
                v.getModeloTrabalho(),
                v.getRequisitos(),
                v.getStatus(),
                v.getPublicadaEm(),
                v.getEncerradaEm(),
                v.getCriadoPor() != null ? v.getCriadoPor().getId() : null,
                v.getCriadoPor() != null ? v.getCriadoPor().getEmail() : null,
                v.getCriadoEm(),
                v.getAtualizadoEm()
        );
    }
}
