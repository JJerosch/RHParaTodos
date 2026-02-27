package proj.paratodos.dto;

import proj.paratodos.domain.Departamento;

import java.time.LocalDateTime;

public record DepartamentoResponse(
        Long id,
        String nome,
        String descricao,
        Long departamentoPaiId,
        String departamentoPaiNome,
        Boolean ativo,
        long funcionarios,
        long cargos,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {

    public static DepartamentoResponse fromEntity(Departamento d, long funcionarios, long cargos, String paiNome) {
        return new DepartamentoResponse(
                d.getId(),
                d.getNome(),
                d.getDescricao(),
                d.getDepartamentoPaiId(),
                paiNome,
                d.getAtivo(),
                funcionarios,
                cargos,
                d.getCriadoEm(),
                d.getAtualizadoEm()
        );
    }
}
