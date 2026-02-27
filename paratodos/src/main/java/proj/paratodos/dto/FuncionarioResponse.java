package proj.paratodos.dto;

import proj.paratodos.domain.Funcionario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FuncionarioResponse(
        Long id,
        String matricula,
        String nomeCompleto,
        String cpf,
        String rg,
        LocalDate dataNascimento,
        String genero,
        String estadoCivil,
        String emailPessoal,
        String emailCorporativo,
        String telefone,
        String celular,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        String banco,
        String agencia,
        String conta,
        String tipoConta,
        String pix,
        Long cargoId,
        String cargoTitulo,
        Long departamentoId,
        String departamentoNome,
        Long gestorId,
        String gestorNome,
        LocalDate dataAdmissao,
        LocalDate dataDesligamento,
        String status,
        String tipoContrato,
        BigDecimal salarioAtual,
        String fotoUrl,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {

    public static FuncionarioResponse fromEntity(Funcionario f) {
        return new FuncionarioResponse(
                f.getId(),
                f.getMatricula(),
                f.getNomeCompleto(),
                f.getCpf(),
                f.getRg(),
                f.getDataNascimento(),
                f.getGenero(),
                f.getEstadoCivil(),
                f.getEmailPessoal(),
                f.getEmailCorporativo(),
                f.getTelefone(),
                f.getCelular(),
                f.getCep(),
                f.getLogradouro(),
                f.getNumero(),
                f.getComplemento(),
                f.getBairro(),
                f.getCidade(),
                f.getEstado(),
                f.getBanco(),
                f.getAgencia(),
                f.getConta(),
                f.getTipoConta(),
                f.getPix(),
                f.getCargo() != null ? f.getCargo().getId() : null,
                f.getCargo() != null ? f.getCargo().getTitulo() : null,
                f.getDepartamento() != null ? f.getDepartamento().getId() : null,
                f.getDepartamento() != null ? f.getDepartamento().getNome() : null,
                f.getGestor() != null ? f.getGestor().getId() : null,
                f.getGestor() != null ? f.getGestor().getNomeCompleto() : null,
                f.getDataAdmissao(),
                f.getDataDesligamento(),
                f.getStatus(),
                f.getTipoContrato(),
                f.getSalarioAtual(),
                f.getFotoUrl(),
                f.getCriadoEm(),
                f.getAtualizadoEm()
        );
    }
}
