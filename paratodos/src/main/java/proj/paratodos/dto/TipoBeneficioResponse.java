package proj.paratodos.dto;

import proj.paratodos.domain.Cargo;
import proj.paratodos.domain.TipoBeneficio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TipoBeneficioResponse(
        Long id,
        String nome,
        String descricao,
        Boolean possuiDescontoFolha,
        BigDecimal valorPadrao,
        Boolean ativo,
        String natureza,
        Boolean incideFerias,
        Boolean incideDecimo,
        long beneficiarios,
        List<CargoRef> cargos,
        LocalDateTime criadoEm
) {

    public record CargoRef(Long id, String titulo) {}

    public static TipoBeneficioResponse fromEntity(TipoBeneficio t, long beneficiarios) {
        List<CargoRef> cargoRefs = t.getCargos() != null
                ? t.getCargos().stream()
                    .map(c -> new CargoRef(c.getId(), c.getTitulo()))
                    .toList()
                : List.of();

        return new TipoBeneficioResponse(
                t.getId(),
                t.getNome(),
                t.getDescricao(),
                t.getPossuiDescontoFolha(),
                t.getValorPadrao(),
                t.getAtivo(),
                t.getNatureza(),
                t.getIncideFerias(),
                t.getIncideDecimo(),
                beneficiarios,
                cargoRefs,
                t.getCriadoEm()
        );
    }
}
