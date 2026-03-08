package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proj.paratodos.domain.Solicitacao;

import java.util.List;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    @Query("""
        SELECT s FROM Solicitacao s
        LEFT JOIN FETCH s.funcionario
        LEFT JOIN FETCH s.solicitante
        LEFT JOIN FETCH s.aprovador
        ORDER BY s.dataSolicitacao DESC
    """)
    List<Solicitacao> findAllWithDetails();

    @Query("""
        SELECT s FROM Solicitacao s
        LEFT JOIN FETCH s.funcionario
        LEFT JOIN FETCH s.solicitante
        LEFT JOIN FETCH s.aprovador
        WHERE s.status = :status
        ORDER BY s.dataSolicitacao DESC
    """)
    List<Solicitacao> findByStatusWithDetails(@Param("status") String status);

    @Query("""
        SELECT s FROM Solicitacao s
        LEFT JOIN FETCH s.funcionario
        LEFT JOIN FETCH s.solicitante
        LEFT JOIN FETCH s.aprovador
        WHERE s.id = :id
    """)
    Solicitacao findByIdWithDetails(@Param("id") Long id);

    long countByStatus(String status);
}
