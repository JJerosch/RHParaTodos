package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proj.paratodos.domain.Vaga;

import java.util.List;

public interface VagaRepository extends JpaRepository<Vaga, Long> {

    @Query("SELECT DISTINCT v FROM Vaga v " +
           "LEFT JOIN FETCH v.departamento " +
           "LEFT JOIN FETCH v.cargo " +
           "LEFT JOIN FETCH v.criadoPor " +
           "LEFT JOIN FETCH v.beneficios " +
           "ORDER BY v.criadoEm DESC")
    List<Vaga> findAllWithDetails();

    @Query("SELECT DISTINCT v FROM Vaga v " +
           "LEFT JOIN FETCH v.departamento " +
           "LEFT JOIN FETCH v.cargo " +
           "LEFT JOIN FETCH v.criadoPor " +
           "LEFT JOIN FETCH v.beneficios " +
           "WHERE v.status = :status " +
           "ORDER BY v.criadoEm DESC")
    List<Vaga> findByStatusWithDetails(@Param("status") String status);

    @Query("SELECT v FROM Vaga v " +
           "LEFT JOIN FETCH v.departamento " +
           "LEFT JOIN FETCH v.cargo " +
           "LEFT JOIN FETCH v.criadoPor " +
           "LEFT JOIN FETCH v.beneficios " +
           "WHERE v.id = :id")
    Vaga findByIdWithDetails(@Param("id") Long id);

    long countByStatus(String status);

    @Query("SELECT COUNT(v) FROM Vaga v WHERE v.departamento.id = :deptId AND v.status IN ('ABERTA','EM_ANDAMENTO')")
    long countActiveByDepartamentoId(@Param("deptId") Long deptId);

    @Query("SELECT v FROM Vaga v WHERE v.cargo.id = :cargoId AND v.status IN ('ABERTA','EM_ANDAMENTO')")
    List<Vaga> findActiveByCargoId(@Param("cargoId") Long cargoId);
}
