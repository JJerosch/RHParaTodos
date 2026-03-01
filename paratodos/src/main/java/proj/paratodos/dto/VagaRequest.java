package proj.paratodos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record VagaRequest(

        @NotBlank(message = "Titulo e obrigatorio")
        @Size(max = 150)
        String titulo,

        String descricao,

        Long departamentoId,

        Long cargoId,

        Integer quantidade,

        String prioridade,

        BigDecimal salarioMin,

        BigDecimal salarioMax,

        String tipoContrato,

        String localTrabalho,

        String modeloTrabalho,

        String requisitos,

        String status
) {}
