package proj.paratodos.dto;

import proj.paratodos.domain.Departamento;

import java.time.LocalDateTime;

public record DepartamentoResponse(
        Long id,
        String nome,
        String descricao,
        Boolean ativo,
        long funcionarios,
        long cargos,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {

    public static DepartamentoResponse fromEntity(Departamento d, long funcionarios, long cargos) {
        return new DepartamentoResponse(
                d.getId(),
                d.getNome(),
                d.getDescricao(),
                d.getAtivo(),
                funcionarios,
                cargos,
                d.getCriadoEm(),
                d.getAtualizadoEm()
        );
    }
}
