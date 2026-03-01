package proj.paratodos.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CandidatoRequest(

        @NotBlank(message = "Nome completo e obrigatorio")
        String nomeCompleto,

        @NotBlank(message = "Email e obrigatorio")
        String email,

        String telefone,

        String linkedinUrl,

        String curriculoUrl,

        String cpf,

        LocalDate dataNascimento,

        String cidade,

        String estado,

        BigDecimal pretensaoSalarial,

        String observacoes
) {}
