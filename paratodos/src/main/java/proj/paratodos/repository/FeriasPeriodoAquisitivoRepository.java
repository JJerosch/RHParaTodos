package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proj.paratodos.domain.FeriasPeriodoAquisitivo;

import java.util.Optional;

public interface FeriasPeriodoAquisitivoRepository extends JpaRepository<FeriasPeriodoAquisitivo, Long> {

    @Query("""
        select f
        from FeriasPeriodoAquisitivo f
        where f.funcionarioId = :funcionarioId
        order by f.dataFim desc
    """)
    Optional<FeriasPeriodoAquisitivo> findTopByFuncionarioIdOrderByDataFimDesc(@Param("funcionarioId") Long funcionarioId);
}