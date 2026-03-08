package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proj.paratodos.domain.PontoJornada;

public interface PontoJornadaRepository extends JpaRepository<PontoJornada, Long> {
}