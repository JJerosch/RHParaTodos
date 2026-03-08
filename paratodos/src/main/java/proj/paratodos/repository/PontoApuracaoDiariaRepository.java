package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proj.paratodos.domain.PontoApuracaoDiaria;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PontoApuracaoDiariaRepository extends JpaRepository<PontoApuracaoDiaria, Long> {

    Optional<PontoApuracaoDiaria> findByFuncionarioIdAndData(Long funcionarioId, LocalDate data);

    List<PontoApuracaoDiaria> findByFuncionarioIdAndDataBetweenOrderByDataDesc(
            Long funcionarioId,
            LocalDate inicio,
            LocalDate fim
    );

    List<PontoApuracaoDiaria> findByDataBetweenOrderByDataDesc(LocalDate inicio, LocalDate fim);

    @Query("""
        select p
        from PontoApuracaoDiaria p
        join fetch p.funcionario f
        where p.data between :inicio and :fim
        order by p.data desc, f.nomeCompleto asc
    """)
    List<PontoApuracaoDiaria> buscarTimesheetAdmin(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query(value = """
        select pad.*
        from ponto_apuracao_diaria pad
        join funcionarios f on f.id = pad.funcionario_id
        where pad.data between :inicio and :fim
          and f.nome_completo ilike :nome
        order by pad.data desc, f.nome_completo asc
    """, nativeQuery = true)
    List<PontoApuracaoDiaria> buscarTimesheetAdminPorNome(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("nome") String nome
    );
}