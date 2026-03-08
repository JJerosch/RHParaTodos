package proj.paratodos.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "ponto_jornadas")
public class PontoJornada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "carga_horaria_diaria", precision = 10, scale = 2, nullable = false)
    private BigDecimal cargaHorariaDiaria;

    @Column(name = "intervalo_minutos", nullable = false)
    private Integer intervaloMinutos;

    @Column(name = "horario_entrada")
    private LocalTime horarioEntrada;

    @Column(name = "horario_saida")
    private LocalTime horarioSaida;

    @Column(nullable = false)
    private Boolean flexivel = false;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
}