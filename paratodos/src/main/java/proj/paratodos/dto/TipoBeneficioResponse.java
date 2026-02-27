package proj.paratodos.dto;

import proj.paratodos.domain.TipoBeneficio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        LocalDateTime criadoEm
) {

    public static TipoBeneficioResponse fromEntity(TipoBeneficio t, long beneficiarios) {
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
                t.getCriadoEm()
        );
    }
}
