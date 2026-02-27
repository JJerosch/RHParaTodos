package proj.paratodos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CargoRequest(

        @NotBlank(message = "Titulo e obrigatorio")
        @Size(max = 100)
        String titulo,

        String descricao,

        @Size(max = 50)
        String nivel,

        Long departamentoId,

        BigDecimal salarioMinimo,

        BigDecimal salarioMaximo,

        Boolean ativo
) {}
