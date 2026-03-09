package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proj.paratodos.domain.PontoOcorrencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PontoOcorrenciaRepository extends JpaRepository<PontoOcorrencia, Long> {

    @Query("""
        select o
        from PontoOcorrencia o
        where o.funcionario.id = :funcionarioId
          and o.tipo = :tipo
          and o.dataFim >= :inicio
          and o.dataInicio <= :fim
        order by o.dataInicio desc, o.id desc
    """)
    List<PontoOcorrencia> findSobrepostas(
            @Param("funcionarioId") Long funcionarioId,
            @Param("tipo") String tipo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("""
        select o
        from PontoOcorrencia o
        where o.funcionario.id = :funcionarioId
          and :data between o.dataInicio and o.dataFim
        order by o.dataInicio desc, o.id desc
    """)
    List<PontoOcorrencia> findAtivasNoDia(
            @Param("funcionarioId") Long funcionarioId,
            @Param("data") LocalDate data
    );

    default Optional<PontoOcorrencia> findPrimeiraAtivaNoDia(Long funcionarioId, LocalDate data) {
        List<PontoOcorrencia> lista = findAtivasNoDia(funcionarioId, data);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    @Query("""
        select o
        from PontoOcorrencia o
        join fetch o.funcionario f
        left join fetch f.cargo
        left join fetch f.departamento
        where o.tipo = 'FERIAS'
          and :data between o.dataInicio and o.dataFim
        order by f.nomeCompleto asc
    """)
    List<PontoOcorrencia> findFeriasAtivasNaData(@Param("data") LocalDate data);

    @Query("""
        select o
        from PontoOcorrencia o
        join fetch o.funcionario f
        where (:funcionarioId is null or f.id = :funcionarioId)
          and o.dataFim >= :inicio
          and o.dataInicio <= :fim
        order by o.dataInicio desc, f.nomeCompleto asc
    """)
    List<PontoOcorrencia> listarPorPeriodo(
            @Param("funcionarioId") Long funcionarioId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
}