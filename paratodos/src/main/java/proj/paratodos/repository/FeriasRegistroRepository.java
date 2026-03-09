package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proj.paratodos.domain.FeriasRegistro;

import java.time.LocalDate;
import java.util.List;

public interface FeriasRegistroRepository extends JpaRepository<FeriasRegistro, Long> {

    @Query("""
        select fr
        from FeriasRegistro fr
        join fetch fr.funcionario f
        left join fetch f.cargo
        left join fetch f.departamento
        where :data between fr.dataInicio and fr.dataFim
        order by f.nomeCompleto asc
    """)
    List<FeriasRegistro> findAtivosNaData(@Param("data") LocalDate data);

    @Query("""
        select count(fr) > 0
        from FeriasRegistro fr
        where fr.funcionario.id = :funcionarioId
          and :data between fr.dataInicio and fr.dataFim
    """)
    boolean existsAtivoNaData(
            @Param("funcionarioId") Long funcionarioId,
            @Param("data") LocalDate data
    );

    @Query("""
        select count(fr) > 0
        from FeriasRegistro fr
        where fr.funcionario.id = :funcionarioId
          and fr.dataFim >= :inicio
          and fr.dataInicio <= :fim
    """)
    boolean existsSobreposicao(
            @Param("funcionarioId") Long funcionarioId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
}