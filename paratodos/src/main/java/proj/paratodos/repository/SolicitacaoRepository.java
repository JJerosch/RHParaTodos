package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proj.paratodos.domain.ReferenciaTipo;
import proj.paratodos.domain.Solicitacao;
import proj.paratodos.domain.StatusSolicitacao;
import proj.paratodos.domain.TipoSolicitacao;

import java.util.List;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    @Query("SELECT s FROM Solicitacao s " +
           "LEFT JOIN FETCH s.solicitante " +
           "LEFT JOIN FETCH s.aprovador " +
           "ORDER BY s.criadoEm DESC")
    List<Solicitacao> findAllWithDetails();

    @Query("SELECT s FROM Solicitacao s " +
           "LEFT JOIN FETCH s.solicitante " +
           "LEFT JOIN FETCH s.aprovador " +
           "WHERE s.status = :status " +
           "ORDER BY s.criadoEm DESC")
    List<Solicitacao> findByStatusWithDetails(@Param("status") StatusSolicitacao status);

    @Query("SELECT s FROM Solicitacao s " +
           "LEFT JOIN FETCH s.solicitante " +
           "LEFT JOIN FETCH s.aprovador " +
           "WHERE s.tipo = :tipo " +
           "ORDER BY s.criadoEm DESC")
    List<Solicitacao> findByTipoWithDetails(@Param("tipo") TipoSolicitacao tipo);

    @Query("SELECT s FROM Solicitacao s " +
           "LEFT JOIN FETCH s.solicitante " +
           "LEFT JOIN FETCH s.aprovador " +
           "WHERE s.status = :status AND s.tipo = :tipo " +
           "ORDER BY s.criadoEm DESC")
    List<Solicitacao> findByStatusAndTipoWithDetails(@Param("status") StatusSolicitacao status,
                                                     @Param("tipo") TipoSolicitacao tipo);

    @Query("SELECT s FROM Solicitacao s " +
           "LEFT JOIN FETCH s.solicitante " +
           "LEFT JOIN FETCH s.aprovador " +
           "WHERE s.id = :id")
    Solicitacao findByIdWithDetails(@Param("id") Long id);

    List<Solicitacao> findByReferenciaTipoAndReferenciaId(ReferenciaTipo referenciaTipo, Long referenciaId);

    long countByStatus(StatusSolicitacao status);

    long countByTipo(TipoSolicitacao tipo);

    long countByStatusAndTipo(StatusSolicitacao status, TipoSolicitacao tipo);

    @Query("SELECT s FROM Solicitacao s " +
           "LEFT JOIN FETCH s.solicitante " +
           "WHERE s.status = :status " +
           "ORDER BY s.criadoEm DESC")
    List<Solicitacao> findByStatusOrderByCriadoEmDesc(@Param("status") StatusSolicitacao status);
}
