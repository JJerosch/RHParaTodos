package proj.paratodos.dto;

import proj.paratodos.domain.Candidato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CandidatoResponse(
        Long id,
        String nomeCompleto,
        String email,
        String telefone,
        String linkedinUrl,
        String curriculoUrl,
        String cpf,
        LocalDate dataNascimento,
        String cidade,
        String estado,
        BigDecimal pretensaoSalarial,
        String observacoes,
        LocalDateTime criadoEm
) {

    public static CandidatoResponse fromEntity(Candidato c) {
        return new CandidatoResponse(
                c.getId(),
                c.getNomeCompleto(),
                c.getEmail(),
                c.getTelefone(),
                c.getLinkedinUrl(),
                c.getCurriculoUrl(),
                c.getCpf(),
                c.getDataNascimento(),
                c.getCidade(),
                c.getEstado(),
                c.getPretensaoSalarial(),
                c.getObservacoes(),
                c.getCriadoEm()
        );
    }
}
