package proj.paratodos.dto;

import proj.paratodos.domain.Cargo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CargoResponse(
        Long id,
        String titulo,
        String descricao,
        String nivel,
        Long departamentoId,
        String departamentoNome,
        BigDecimal salarioMinimo,
        BigDecimal salarioMaximo,
        Boolean ativo,
        long ocupantes,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {

    public static CargoResponse fromEntity(Cargo c, long ocupantes) {
        return new CargoResponse(
                c.getId(),
                c.getTitulo(),
                c.getDescricao(),
                c.getNivel(),
                c.getDepartamento() != null ? c.getDepartamento().getId() : null,
                c.getDepartamento() != null ? c.getDepartamento().getNome() : null,
                c.getSalarioMinimo(),
                c.getSalarioMaximo(),
                c.getAtivo(),
                ocupantes,
                c.getCriadoEm(),
                c.getAtualizadoEm()
        );
    }
}
