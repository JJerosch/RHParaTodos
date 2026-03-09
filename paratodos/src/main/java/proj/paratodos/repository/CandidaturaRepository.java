package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proj.paratodos.domain.Candidatura;

import java.util.List;

public interface CandidaturaRepository extends JpaRepository<Candidatura, Long> {

    @Query("SELECT c FROM Candidatura c " +
           "LEFT JOIN FETCH c.vaga " +
           "LEFT JOIN FETCH c.candidato " +
           "WHERE c.vaga.id = :vagaId " +
           "ORDER BY c.criadoEm DESC")
    List<Candidatura> findByVagaIdWithDetails(@Param("vagaId") Long vagaId);

    @Query("SELECT c FROM Candidatura c " +
           "LEFT JOIN FETCH c.vaga v " +
           "LEFT JOIN FETCH v.departamento " +
           "LEFT JOIN FETCH v.cargo " +
           "LEFT JOIN FETCH c.candidato " +
           "WHERE c.id = :id")
    Candidatura findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT COUNT(c) FROM Candidatura c WHERE c.vaga.id = :vagaId AND c.etapa = 'CONTRATADO'")
    long countContratadosByVagaId(@Param("vagaId") Long vagaId);

    long countByVagaId(Long vagaId);

    long countByEtapa(String etapa);

    boolean existsByVagaIdAndCandidatoId(Long vagaId, Long candidatoId);
}
