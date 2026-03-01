package proj.paratodos.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "candidatos")
public class Candidato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @Column(nullable = false)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "curriculo_url", length = 500)
    private String curriculoUrl;

    @Column(length = 14)
    private String cpf;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Column(name = "pretensao_salarial", precision = 10, scale = 2)
    private BigDecimal pretensaoSalarial;

    @Column(columnDefinition = "text")
    private String observacoes;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }
}
