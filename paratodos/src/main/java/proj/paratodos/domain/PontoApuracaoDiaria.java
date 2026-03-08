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
@Table(name = "ponto_apuracao_diaria")
public class PontoApuracaoDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "horas_trabalhadas", precision = 10, scale = 2, nullable = false)
    private BigDecimal horasTrabalhadas = BigDecimal.ZERO;

    @Column(name = "horas_extras", precision = 10, scale = 2, nullable = false)
    private BigDecimal horasExtras = BigDecimal.ZERO;

    @Column(name = "horas_faltantes", precision = 10, scale = 2, nullable = false)
    private BigDecimal horasFaltantes = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean fechado = false;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        if (horasTrabalhadas == null) horasTrabalhadas = BigDecimal.ZERO;
        if (horasExtras == null) horasExtras = BigDecimal.ZERO;
        if (horasFaltantes == null) horasFaltantes = BigDecimal.ZERO;
        if (fechado == null) fechado = false;
    }
}