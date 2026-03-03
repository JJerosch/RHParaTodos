package proj.paratodos.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proj.paratodos.domain.Funcionario;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    @Query("""
        SELECT f FROM Funcionario f
        LEFT JOIN FETCH f.cargo
        LEFT JOIN FETCH f.departamento
        LEFT JOIN FETCH f.gestor
        WHERE f.usuario.id = :usuarioId
    """)
    Optional<Funcionario> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    boolean existsByCpf(String cpf);

    boolean existsByMatricula(String matricula);

    boolean existsByEmailCorporativo(String emailCorporativo);

    @Query(value = """
        SELECT f FROM Funcionario f
        LEFT JOIN FETCH f.cargo
        LEFT JOIN FETCH f.departamento
        LEFT JOIN FETCH f.gestor
        WHERE (:search IS NULL
            OR LOWER(f.nomeCompleto) LIKE LOWER(CONCAT('%', :search, '%'))
            OR f.matricula LIKE CONCAT('%', :search, '%')
            OR f.cpf LIKE CONCAT('%', :search, '%'))
        AND (:departamentoId IS NULL OR f.departamento.id = :departamentoId)
        AND (:status IS NULL OR f.status = :status)
    """,
    countQuery = """
        SELECT COUNT(f) FROM Funcionario f
        WHERE (:search IS NULL
            OR LOWER(f.nomeCompleto) LIKE LOWER(CONCAT('%', :search, '%'))
            OR f.matricula LIKE CONCAT('%', :search, '%')
            OR f.cpf LIKE CONCAT('%', :search, '%'))
        AND (:departamentoId IS NULL OR f.departamento.id = :departamentoId)
        AND (:status IS NULL OR f.status = :status)
    """)
    Page<Funcionario> search(
            @Param("search") String search,
            @Param("departamentoId") Long departamentoId,
            @Param("status") String status,
            Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT COUNT(f) FROM Funcionario f WHERE f.status = 'DESLIGADO' AND f.dataDesligamento >= :desde")
    long countDesligadosDesde(@Param("desde") LocalDate desde);

    @Query("SELECT COUNT(f) FROM Funcionario f WHERE f.dataAdmissao >= :desde")
    long countContratadosDesde(@Param("desde") LocalDate desde);

    @Query("""
        SELECT f FROM Funcionario f
        LEFT JOIN FETCH f.cargo
        LEFT JOIN FETCH f.departamento
        WHERE f.dataAdmissao >= :desde
        ORDER BY f.dataAdmissao DESC
    """)
    List<Funcionario> findContratadosRecentes(@Param("desde") LocalDate desde);

    @Query("""
        SELECT f FROM Funcionario f
        LEFT JOIN FETCH f.cargo
        LEFT JOIN FETCH f.departamento
        WHERE f.status = 'ATIVO'
          AND EXTRACT(MONTH FROM f.dataNascimento) = :mes
        ORDER BY EXTRACT(DAY FROM f.dataNascimento) ASC
    """)
    List<Funcionario> findAniversariantesMes(@Param("mes") int mes);

    @Query("""
        SELECT f FROM Funcionario f
        LEFT JOIN FETCH f.cargo
        LEFT JOIN FETCH f.departamento
        WHERE f.status = 'ATIVO'
          AND f.cargo IS NOT NULL
          AND f.departamento IS NOT NULL
        ORDER BY f.nomeCompleto ASC
    """)
    List<Funcionario> findAtivosComCargoEDepartamento();

    @Query("""
        SELECT f FROM Funcionario f
        LEFT JOIN FETCH f.cargo
        LEFT JOIN FETCH f.departamento
        WHERE f.status = 'ATIVO'
          AND f.departamento.id = :departamentoId
        ORDER BY f.cargo.titulo ASC, f.nomeCompleto ASC
    """)
    List<Funcionario> findAtivosByDepartamentoId(@Param("departamentoId") Long departamentoId);
}
