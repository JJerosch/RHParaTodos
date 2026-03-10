package proj.paratodos.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "vagas")
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(columnDefinition = "text")
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @Column(nullable = false)
    private Integer quantidade = 1;

    @Column(length = 20)
    private String prioridade = "MEDIA";

    @Column(name = "salario_min", precision = 10, scale = 2)
    private BigDecimal salarioMin;

    @Column(name = "salario_max", precision = 10, scale = 2)
    private BigDecimal salarioMax;

    @Column(name = "tipo_contrato", length = 30)
    private String tipoContrato;

    @Column(name = "local_trabalho", length = 100)
    private String localTrabalho;

    @Column(name = "modelo_trabalho", length = 20)
    private String modeloTrabalho = "PRESENCIAL";

    @Column(columnDefinition = "text")
    private String requisitos;

    @Column(length = 20)
    private String status = "RASCUNHO";

    @Column(name = "publicada_em")
    private LocalDateTime publicadaEm;

    @Column(name = "encerrada_em")
    private LocalDateTime encerradaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por")
    private Usuario criadoPor;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "vagas_beneficios",
            joinColumns = @JoinColumn(name = "vaga_id"),
            inverseJoinColumns = @JoinColumn(name = "tipo_beneficio_id")
    )
    private Set<TipoBeneficio> beneficios = new HashSet<>();

    @PrePersist
    void prePersist() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }
}
