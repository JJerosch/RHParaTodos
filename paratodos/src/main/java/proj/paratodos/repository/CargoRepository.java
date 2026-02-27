package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proj.paratodos.domain.Cargo;

import java.util.List;

public interface CargoRepository extends JpaRepository<Cargo, Long> {

    List<Cargo> findByAtivoTrueOrderByTituloAsc();

    List<Cargo> findByDepartamentoIdAndAtivoTrueOrderByTituloAsc(Long departamentoId);

    List<Cargo> findAllByOrderByTituloAsc();

    @Query("""
        SELECT c FROM Cargo c
        LEFT JOIN FETCH c.departamento
        WHERE (:search IS NULL OR LOWER(c.titulo) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:departamentoId IS NULL OR c.departamento.id = :departamentoId)
        AND (:nivel IS NULL OR c.nivel = :nivel)
        ORDER BY c.titulo ASC
    """)
    List<Cargo> search(
            @Param("search") String search,
            @Param("departamentoId") Long departamentoId,
            @Param("nivel") String nivel);

    @Query("SELECT COUNT(f) FROM Funcionario f WHERE f.cargo.id = :cargoId")
    long countFuncionariosByCargoId(@Param("cargoId") Long cargoId);
}
