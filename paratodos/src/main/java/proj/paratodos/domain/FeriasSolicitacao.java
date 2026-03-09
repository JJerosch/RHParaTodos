package proj.paratodos.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ferias_solicitacoes")
public class FeriasSolicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Column(name = "periodo_aquisitivo_id", nullable = false)
    private Long periodoAquisitivoId;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "dias_solicitados", nullable = false)
    private Integer diasSolicitados;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "solicitado_em", updatable = false)
    private LocalDateTime solicitadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprovado_por")
    private Usuario aprovadoPor;

    @Column(name = "aprovado_em")
    private LocalDateTime aprovadoEm;

    @PrePersist
    void prePersist() {
        if (solicitadoEm == null) {
            solicitadoEm = LocalDateTime.now();
        }
        if (status != null) {
            status = status.trim().toUpperCase();
        }
    }
}