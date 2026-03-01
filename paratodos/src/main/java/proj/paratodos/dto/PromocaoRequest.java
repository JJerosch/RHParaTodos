package proj.paratodos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PromocaoRequest(

        @NotNull(message = "Funcionario e obrigatorio")
        Long funcionarioId,

        Long cargoNovoId,

        Long departamentoNovoId,

        BigDecimal salarioNovo,

        @NotBlank(message = "Motivo e obrigatorio")
        String motivo,

        String tipo
) {}
