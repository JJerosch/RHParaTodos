package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proj.paratodos.domain.PontoCalendario;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PontoCalendarioRepository extends JpaRepository<PontoCalendario, Long> {

    Optional<PontoCalendario> findByData(LocalDate data);

    List<PontoCalendario> findByDataBetweenOrderByDataAsc(LocalDate inicio, LocalDate fim);

    void deleteByData(LocalDate data);
}