package proj.paratodos.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "promocoes")
public class Promocao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_atual_id")
    private Cargo cargoAtual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_novo_id")
    private Cargo cargoNovo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_atual_id")
    private Departamento departamentoAtual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_novo_id")
    private Departamento departamentoNovo;

    @Column(name = "salario_atual", precision = 10, scale = 2)
    private BigDecimal salarioAtual;

    @Column(name = "salario_novo", precision = 10, scale = 2)
    private BigDecimal salarioNovo;

    @Column(nullable = false, columnDefinition = "text")
    private String motivo;

    @Column(length = 30)
    private String tipo = "PROMOCAO";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprovador_id")
    private Usuario aprovador;

    @Column(length = 20)
    private String status = "PENDENTE";

    @Column(name = "data_solicitacao")
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_decisao")
    private LocalDateTime dataDecisao;

    @Column(name = "observacao_aprovador", columnDefinition = "text")
    private String observacaoAprovador;

    @PrePersist
    void prePersist() {
        if (dataSolicitacao == null) {
            dataSolicitacao = LocalDateTime.now();
        }
    }
}
