package proj.paratodos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FuncionarioRequest(

        @NotBlank(message = "Matricula e obrigatoria")
        @Size(max = 20)
        String matricula,

        @NotBlank(message = "Nome completo e obrigatorio")
        @Size(max = 255)
        String nomeCompleto,

        @NotBlank(message = "CPF e obrigatorio")
        @Size(max = 14)
        String cpf,

        @Size(max = 20)
        String rg,

        @NotNull(message = "Data de nascimento e obrigatoria")
        LocalDate dataNascimento,

        String genero,
        String estadoCivil,
        String emailPessoal,
        String emailCorporativo,
        String telefone,
        String celular,

        // Endereco
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,

        // Dados bancarios
        String banco,
        String agencia,
        String conta,
        String tipoConta,
        String pix,

        // Contato de emergencia
        String emergenciaNome,
        String emergenciaParentesco,
        String emergenciaTelefone,

        // Dados profissionais
        Long cargoId,
        Long departamentoId,
        Long gestorId,

        @NotNull(message = "Data de admissao e obrigatoria")
        LocalDate dataAdmissao,

        LocalDate dataDesligamento,
        String status,
        String tipoContrato,
        BigDecimal salarioAtual
) {}
