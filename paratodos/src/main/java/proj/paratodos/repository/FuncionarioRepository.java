package proj.paratodos.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proj.paratodos.domain.Funcionario;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByMatricula(String matricula);

    boolean existsByEmailCorporativo(String emailCorporativo);

    @Query("""
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
    """)
    Page<Funcionario> search(
            @Param("search") String search,
            @Param("departamentoId") Long departamentoId,
            @Param("status") String status,
            Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT COUNT(f) FROM Funcionario f WHERE f.status = 'DESLIGADO' AND f.dataDesligamento >= :desde")
    long countDesligadosDesde(@Param("desde") java.time.LocalDate desde);
}
