package proj.paratodos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record TipoBeneficioRequest(

        @NotBlank(message = "Nome e obrigatorio")
        @Size(max = 100)
        String nome,

        String descricao,

        Boolean possuiDescontoFolha,

        BigDecimal valorPadrao,

        Boolean ativo,

        @NotNull(message = "Natureza e obrigatoria")
        String natureza, // PROVENTO, DESCONTO, INFORMATIVO

        Boolean incideFerias,

        Boolean incideDecimo,

        List<Long> cargoIds
) {}
