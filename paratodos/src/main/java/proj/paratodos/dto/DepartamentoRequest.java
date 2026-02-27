package proj.paratodos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartamentoRequest(

        @NotBlank(message = "Nome e obrigatorio")
        @Size(max = 100)
        String nome,

        String descricao,

        Long departamentoPaiId,

        Boolean ativo
) {}
