package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proj.paratodos.domain.Departamento;

import java.util.List;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

    List<Departamento> findByAtivoTrueOrderByNomeAsc();

    List<Departamento> findAllByOrderByNomeAsc();

    long countByAtivoTrue();

    boolean existsByNome(String nome);

    @Query("SELECT COUNT(f) FROM Funcionario f WHERE f.departamento.id = :depId")
    long countFuncionariosByDepartamentoId(@Param("depId") Long depId);

    @Query("SELECT COUNT(c) FROM Cargo c WHERE c.departamento.id = :depId")
    long countCargosByDepartamentoId(@Param("depId") Long depId);

    @Query("SELECT COUNT(f) FROM Funcionario f WHERE f.departamento IS NOT NULL")
    long countTotalFuncionariosComDepartamento();
}
