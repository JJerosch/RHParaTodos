package proj.paratodos.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ponto_ocorrencias")
public class PontoOcorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(columnDefinition = "text")
    private String observacao;

    @Column(name = "abona_dia", nullable = false)
    private Boolean abonaDia = true;

    @Column(name = "bloqueia_marcacao", nullable = false)
    private Boolean bloqueiaMarcacao = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        if (abonaDia == null) {
            abonaDia = true;
        }
        if (bloqueiaMarcacao == null) {
            bloqueiaMarcacao = true;
        }
        if (tipo != null) {
            tipo = tipo.trim().toUpperCase();
        }
    }
}