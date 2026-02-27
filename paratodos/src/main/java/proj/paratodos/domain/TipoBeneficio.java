package proj.paratodos.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tipos_beneficios")
public class TipoBeneficio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    private String descricao;

    @Column(name = "possui_desconto_folha")
    private Boolean possuiDescontoFolha = true;

    @Column(name = "valor_padrao", precision = 10, scale = 2)
    private BigDecimal valorPadrao;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false, length = 20)
    private String natureza; // PROVENTO, DESCONTO, INFORMATIVO

    @Column(name = "incide_ferias")
    private Boolean incideFerias = false;

    @Column(name = "incide_decimo")
    private Boolean incideDecimo = false;

    @PrePersist
    void prePersist() {
        criadoEm = LocalDateTime.now();
    }
}
