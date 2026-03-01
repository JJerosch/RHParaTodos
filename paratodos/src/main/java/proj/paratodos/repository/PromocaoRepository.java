package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proj.paratodos.domain.Promocao;

import java.util.List;

public interface PromocaoRepository extends JpaRepository<Promocao, Long> {

    @Query("SELECT p FROM Promocao p " +
           "LEFT JOIN FETCH p.funcionario " +
           "LEFT JOIN FETCH p.cargoAtual " +
           "LEFT JOIN FETCH p.cargoNovo " +
           "LEFT JOIN FETCH p.departamentoAtual " +
           "LEFT JOIN FETCH p.departamentoNovo " +
           "LEFT JOIN FETCH p.solicitante " +
           "LEFT JOIN FETCH p.aprovador " +
           "ORDER BY p.dataSolicitacao DESC")
    List<Promocao> findAllWithDetails();

    @Query("SELECT p FROM Promocao p " +
           "LEFT JOIN FETCH p.funcionario " +
           "LEFT JOIN FETCH p.cargoAtual " +
           "LEFT JOIN FETCH p.cargoNovo " +
           "LEFT JOIN FETCH p.departamentoAtual " +
           "LEFT JOIN FETCH p.departamentoNovo " +
           "LEFT JOIN FETCH p.solicitante " +
           "LEFT JOIN FETCH p.aprovador " +
           "WHERE p.status = :status " +
           "ORDER BY p.dataSolicitacao DESC")
    List<Promocao> findByStatusWithDetails(@Param("status") String status);

    long countByStatus(String status);

    @Query("SELECT p FROM Promocao p " +
           "LEFT JOIN FETCH p.funcionario " +
           "LEFT JOIN FETCH p.cargoAtual " +
           "LEFT JOIN FETCH p.cargoNovo " +
           "LEFT JOIN FETCH p.departamentoAtual " +
           "LEFT JOIN FETCH p.departamentoNovo " +
           "LEFT JOIN FETCH p.solicitante " +
           "LEFT JOIN FETCH p.aprovador " +
           "WHERE p.id = :id")
    Promocao findByIdWithDetails(@Param("id") Long id);
}
