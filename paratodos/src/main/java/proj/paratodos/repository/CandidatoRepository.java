package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proj.paratodos.domain.Candidato;

import java.util.Optional;

public interface CandidatoRepository extends JpaRepository<Candidato, Long> {

    Optional<Candidato> findByEmail(String email);

    boolean existsByEmail(String email);
}
